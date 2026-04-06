package net.portalmod.common.sorted.faithplate;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.portalmod.PortalMod;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.ModUtil;

public class FaithPlateTER extends TileEntityRenderer<FaithPlateTileEntity> {
    public static final ResourceLocation TEXTURE_BLUE = new ResourceLocation(PortalMod.MODID, "entity/faithplate");
    public static final ResourceLocation TEXTURE_ORANGE = new ResourceLocation(PortalMod.MODID, "entity/faithplate_active");
    public static RenderMaterial MATERIAL_BLUE;
    public static RenderMaterial MATERIAL_ORANGE;
    private final FaithPlatePlateModel plateModel;
    public static BlockPos selected;

    public FaithPlateTER(TileEntityRendererDispatcher terd) {
        super(terd);
        plateModel = new FaithPlatePlateModel();
        MATERIAL_BLUE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, TEXTURE_BLUE);
        MATERIAL_ORANGE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, TEXTURE_ORANGE);
    }

    private void renderPlate(FaithPlateTileEntity be, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int combinedOverlay) {
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();
        boolean onWall = state.getValue(FaithPlateBlock.FACE) == FaithPlateBlock.Face.WALL;

        matrixStack.pushPose();

        matrixStack.translate(.5, .5, .5);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees((onWall ? 0 : 180) - state.getValue(FaithPlateBlock.FACING).toYRot()));
        if(onWall) {
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        }
        matrixStack.translate(-.5, -.5, -.5);

        matrixStack.translate(.5, 1, 0);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
        matrixStack.translate(0, -1.5, 0);

        IVertexBuilder ivertexbuilder = be.isEnabled() && be.getCooldown() < 2
                ? MATERIAL_ORANGE.buffer(renderBuffer, RenderType::entityTranslucent)
                : MATERIAL_BLUE.buffer(renderBuffer, RenderType::entityTranslucent);

        int light = WorldRenderer.getLightColor(be.getLevel(),
                onWall ? pos.relative(state.getValue(FaithPlateBlock.FACING)) : pos.above());

        plateModel.render(be, plateModel.bone, matrixStack, ivertexbuilder, light, combinedOverlay, new Colour(0xFFFFFFFF), false);
        plateModel.render(be, plateModel.bb_main, matrixStack, ivertexbuilder, light, combinedOverlay, new Colour(0xFFFFFFFF), false);

        matrixStack.popPose();
    }

    private void renderTrigger(FaithPlateTileEntity be, MatrixStack matrixStack) {
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();
        Block block = state.getBlock();

        IRenderTypeBuffer renderTypeBuffer = Minecraft.getInstance().levelRenderer.renderBuffers.outlineBufferSource();
        IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(RenderType.lines());

        if (!(block instanceof FaithPlateBlock)) return;

        Vec3 normal = new Vec3(((FaithPlateBlock) block).getNormal(state).getNormal()).mul(.001);

        matrixStack.pushPose();
        matrixStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        matrixStack.translate(normal.x, normal.y, normal.z);

        WorldRenderer.renderLineBox(matrixStack, vertexBuilder,
                be.getTrigger(), 1, 1, 1, 1);

        matrixStack.popPose();
    }

    private void renderPath(FaithPlateTileEntity be, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, Vector3d absoluteTargetBlockPos, Direction targetFace, int overlay) {
        BlockState state = be.getBlockState();
        Vec3 normal = new Vec3(targetFace.getNormal()).mul(.5);
        Vec3 absoluteTargetPos = new Vec3(absoluteTargetBlockPos).add(.5).add(normal);

        boolean onWall = state.getValue(FaithPlateBlock.FACE) == FaithPlateBlock.Face.WALL;
        Direction plateDirection = onWall ? state.getValue(FaithPlateBlock.FACING) : Direction.UP;
        Vec3 relativeStartPoint = new Vec3(.5).add(new Vec3(plateDirection.getNormal()).mul(.5));

        BlockPos plateBlockPos = be.getBlockPos();
        Vec3 platePos = new Vec3(plateBlockPos).add(relativeStartPoint);
        Vec3 relativeTargetPos = absoluteTargetPos.clone().sub(platePos);

        FaithPlateParabola parabola = new FaithPlateParabola(relativeTargetPos.to3d(), be.getPredictedHeight(absoluteTargetBlockPos));
        IVertexBuilder lineBuffer = renderBuffer.getBuffer(RenderType.lines());

        matrixStack.pushPose();
        matrixStack.translate(relativeStartPoint.x, relativeStartPoint.y, relativeStartPoint.z);
        Matrix4f matrix4f = matrixStack.last().pose();

        if(parabola.isVertical()) {
            vertex(lineBuffer, matrix4f, 0, 0, 0);
            vertex(lineBuffer, matrix4f, 0, be.getPredictedHeight(absoluteTargetBlockPos), 0);
            matrixStack.popPose();
            return;
        }

        double a = parabola.getA();
        double b = parabola.getB();

        double horizontalDistance = relativeStartPoint.clone().mul(1, 0, 1).sub(relativeTargetPos.clone().mul(1, 0, 1)).magnitude();

        // Always use around 20 line segments
        final float increment = Math.max((float) horizontalDistance / 20, 0.05f);

        for(float i = 0;; i += increment) {
            float x = (float)(i * parabola.getComponentX());
            float z = (float)(i * parabola.getComponentZ());
            float y = (float)(a * i * i + b * i);

            float i2 = i + increment;
            float x2 = (float)(i2 * parabola.getComponentX());
            float z2 = (float)(i2 * parabola.getComponentZ());
            float y2 = (float)(a * i2 * i2 + b * i2);

            Vec3 next = new Vec3(x2, y2, z2).add(platePos);
            Vec3 targetToPlateNormal = platePos.clone().sub(absoluteTargetPos).normalize();
            Vec3 nextToPlateNormal = next.clone().sub(absoluteTargetPos).normalize();
            targetToPlateNormal.y = 0;
            nextToPlateNormal.y = 0;

            if (targetToPlateNormal.dot(nextToPlateNormal) < 0 || be.getBlockPos().getY() + y < -1000) {
                vertex(lineBuffer, matrix4f, x, y, z);
                vertex(lineBuffer, matrix4f, (float) relativeTargetPos.x, (float) relativeTargetPos.y, (float) relativeTargetPos.z);
                matrixStack.popPose();
                return;
            }

            vertex(lineBuffer, matrix4f, x, y, z);
            vertex(lineBuffer, matrix4f, x2, y2, z2);
        }
    }

    private void vertex(IVertexBuilder lineBuffer, Matrix4f matrix4f, float x, float y, float z) {
        lineBuffer.vertex(matrix4f, x, y, z).color(1, 1, 1, 1f).endVertex();
    }

    private void renderTarget(FaithPlateTileEntity be, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, Vector3d pos, Direction face, int light, int overlay) {
        float distance = (float)Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(new Vec3(pos).add(be.getBlockPos()).to3d()).dot(new Vec3(face).to3d());
        distance = (float)Math.min(0.0001 + Math.max(0.1 / 200 * (distance - 5), 0), 0.1);

        matrixStack.pushPose();
        matrixStack.translate(pos.x(), pos.y(), pos.z());
        renderBuffer.getBuffer(RenderType.cutout()).putBulkData(matrixStack.last(),
                new FaithPlateTargetBakedModel().getQuad(face, distance),
                1, 1, 1, light, overlay);
        matrixStack.popPose();

        pos = pos.add(new Vec3(face).to3d());

        matrixStack.pushPose();
        matrixStack.translate(pos.x(), pos.y(), pos.z());
        renderBuffer.getBuffer(RenderType.cutout()).putBulkData(matrixStack.last(),
                new FaithPlateTargetBakedModel().getQuad(face.getOpposite(), distance),
                1, 1, 1, light, overlay);
        matrixStack.popPose();
    }

    private void renderPointedPath(FaithPlateTileEntity be, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int overlay) {
        PlayerEntity player = Minecraft.getInstance().player; // TODO check functionality in multiplayer

        if (!be.getBlockPos().equals(selected) || player == null || player.noPhysics) return;

        Item mainHandItem = player.getItemInHand(Hand.MAIN_HAND).getItem();
        Item offHandItem = player.getItemInHand(Hand.OFF_HAND).getItem();

        if (!(mainHandItem == ItemInit.WRENCH.get() || offHandItem == ItemInit.WRENCH.get())) return;

        BlockRayTraceResult rayHit = ModUtil.rayTraceBlock(player, be.getLevel(), 64);

        if (rayHit.getType() == RayTraceResult.Type.MISS) return;

        Direction targetFace = rayHit.getDirection();

        Vector3d absoluteTargetPos = rayHit.getLocation();
        absoluteTargetPos = WrenchItem.getTargetPos(targetFace, absoluteTargetPos);

        BlockPos plateBlockPos = be.getBlockPos();
        Vector3d renderTargetPos = absoluteTargetPos.subtract(Vector3d.atLowerCornerOf(plateBlockPos));
        int targetBlockLight = getTargetLight(be.getLevel(), Vector3d.atCenterOf(rayHit.getBlockPos()), targetFace);

        renderPath(be, matrixStack, renderBuffer, absoluteTargetPos, rayHit.getDirection(), overlay);
        renderTarget(be, matrixStack, renderBuffer, renderTargetPos, targetFace, targetBlockLight, overlay);
    }

    private int getTargetLight(World level, Vector3d pos, Direction face) {
        return WorldRenderer.getLightColor(level, new BlockPos(
                pos.add(Vector3d.atLowerCornerOf(face.getNormal()))
        ));
    }
    
    @Override
    public void render(FaithPlateTileEntity be, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light, int overlay) {
        renderPlate(be, matrixStack, renderBuffer, overlay);

        renderPointedPath(be, matrixStack, renderBuffer, overlay);

        boolean holdingWrench = Minecraft.getInstance().player != null && WrenchItem.holdingWrench(Minecraft.getInstance().player);
        if (Minecraft.getInstance().options.renderDebug && holdingWrench) {
            renderTrigger(be, matrixStack);
        }

        if (be.getTargetFace() == null || be.getTargetPos() == null || be.getBlockPos().equals(selected)) return;

        if (Minecraft.getInstance().options.renderDebug && holdingWrench) {
            renderPath(be, matrixStack, renderBuffer, be.getTargetPos().add(Vector3d.atLowerCornerOf(be.getBlockPos())), be.getTargetFace(), overlay);
        }

        Vector3d absoluteTargetPos = be.getTargetPos().add(Vector3d.atCenterOf(be.getBlockPos()));
        renderTarget(be, matrixStack, renderBuffer, be.getTargetPos(), be.getTargetFace(),
                    getTargetLight(be.getLevel(), absoluteTargetPos, be.getTargetFace()), overlay);

//        FaithPlateParabola parabola = new FaithPlateParabola(new Vec3(be.getTargetPos()).to3d(), be.getHeight());
//        BlockRayTraceResult tr = parabola.findFirstBlockHit(be.getLevel(), be);

//        if (tr != null) {
//            Direction face = tr.getDirection().getOpposite();
//            BlockPos pos = tr.getBlockPos();
//
//            if ((selected == null || !selected.equals(be.getBlockPos()))) {
//                renderTarget(be, matrixStack, renderBuffer, Vector3d.atLowerCornerOf(pos), face,
//                        getTargetLight(be.getLevel(), Vector3d.atLowerCornerOf(pos.offset(be.getBlockPos())), face), overlay);
//            }
//        } else {
//            renderTarget(be, matrixStack, renderBuffer, Vector3d.atLowerCornerOf(be.getBlockPos()), be.getTargetFace(),
//                    getTargetLight(be.getLevel(), be.getTargetPos().add(Vector3d.atLowerCornerOf(be.getBlockPos())), be.getTargetFace()), overlay);
//        }
    }

    public Model getPlateModel() {
        return this.plateModel;
    }

    @Override
    public boolean shouldRenderOffScreen(FaithPlateTileEntity be) {
        return true;
    }
}