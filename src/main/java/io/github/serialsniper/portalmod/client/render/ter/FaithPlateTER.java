package io.github.serialsniper.portalmod.client.render.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.serialsniper.portalmod.client.render.model.FaithPlateTargetBakedModel;
import io.github.serialsniper.portalmod.common.blockentities.FaithPlateTileEntity;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.util.FaithPlateParabola;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndRodBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class FaithPlateTER extends TileEntityRenderer<FaithPlateTileEntity> {
    public static BlockPos selected;

    public FaithPlateTER(TileEntityRendererDispatcher terd) {
        super(terd);
    }

    @Override
    public void render(FaithPlateTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int combinedLight, int combinedOverlay) {
        if(selected == null || !tileEntity.getBlockPos().equals(selected))
            return;

        Item mainHandItem = Minecraft.getInstance().player.getItemInHand(Hand.MAIN_HAND).getItem();
        Item offHandItem = Minecraft.getInstance().player.getItemInHand(Hand.OFF_HAND).getItem();

        if(!(mainHandItem == ItemInit.WRENCH.get() || offHandItem == ItemInit.WRENCH.get()))
            return;

        Vector3d rayPath = Minecraft.getInstance().player.getViewVector(0).scale(64);
        Vector3d from = Minecraft.getInstance().player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
        BlockRayTraceResult rayHit = tileEntity.getLevel().clip(rayCtx);

        if(rayHit.getType() == RayTraceResult.Type.MISS)
            return;

//        Vector3i normal = rayHit.getDirection().getNormal();
//        Vector3i pos = rayHit.getBlockPos();
//        Vector3i thisPos = tileEntity.getBlockPos();
//        Vector3d hit = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(normal.getX() * .5, normal.getY() * .5, normal.getZ() * .5);
//
//        Vector3d offsetVec = hit.subtract(new Vector3d(thisPos.getX(), thisPos.getY(), thisPos.getZ()).add(.5, 0, .5));

        Direction hitDirection = rayHit.getDirection();
        Vector3i hitNormal = hitDirection.getNormal();
        Vector3d hitNormalDouble = new Vector3d(hitNormal.getX() * .5, hitNormal.getY() * .5, hitNormal.getZ() * .5);

        Vector3i hitPos = rayHit.getBlockPos();
        Vector3d hitPosDouble = new Vector3d(hitPos.getX(), hitPos.getY(), hitPos.getZ()).add(.5, .5, .5).add(hitNormalDouble);

        Vector3i thisPos = tileEntity.getBlockPos();
        Vector3d thisPosDouble = new Vector3d(thisPos.getX(), thisPos.getY(), thisPos.getZ()).add(.5, 1, .5);

        Vector3d offset = hitPosDouble.subtract(thisPosDouble);


//        BlockPos offset = rayHit.getBlockPos().subtract(tileEntity.getBlockPos());
//        Vector3d offsetVec = new Vector3d(offset.getX(), offset.getY(), offset.getZ());
        FaithPlateParabola parabola = new FaithPlateParabola(offset, Double.NEGATIVE_INFINITY);

//        BlockPos pos = tileEntity.getBlockPos();
//        Vector3d playerPos = Minecraft.getInstance().player.getPosition(partialTicks);
//        FaithPlateParabola parabola = FaithPlateParabola.get(playerPos.subtract(pos.getX() + .5d, pos.getY() + 1d, pos.getZ() + .5d), Double.NEGATIVE_INFINITY);

        double a = parabola.getA();
        double b = parabola.getB();

        if(Double.isNaN(a)) {
            a = -1;
            b = 1;
        }

//        double a = -0.0102913684953;
//        double b = 0.600860619028;

        IVertexBuilder vertexBuilder = renderBuffer.getBuffer(RenderType.lines());
//        IVertexBuilder vertexBuilder2 = renderBuffer.getBuffer(RenderType.solid());

        matrixStack.pushPose();
        matrixStack.translate(0.5f, 1, 0.5f);
        Matrix4f matrix4f = matrixStack.last().pose();

//        float distance = (float)Math.sqrt(offset.x * offset.x + offset.z * offset.z);
//
//        float increment = MathHelper.clamp(10f / (float)Math.sqrt(offset.x * offset.x + offset.z * offset.z), .01f, .9f);
        final float increment = .1f;

        for(float i = 0;; i += increment) {
            float x = (float)(i * parabola.getComponentX());
            float z = (float)(i * parabola.getComponentZ());
            float y = (float)(a * i * i + b * i);

            BlockPos pos = tileEntity.getBlockPos().offset(0.5f + x, 1 + y, 0.5f + z);
//            BlockState state = tileEntity.getLevel().getBlockState(pos);
//            BlockState thisState = tileEntity.getBlockState();

            if(pos.equals(new BlockPos(hitPos))) {
                matrixStack.popPose();
                BlockPos offset2 = pos.subtract(thisPos);
                matrixStack.translate(offset2.getX(), offset2.getY(), offset2.getZ());

//                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(Minecraft.getInstance().level, new FaithPlateTargetBakedModel(), Blocks.AIR.defaultBlockState(), offset2, matrixStack, vertexBuilder, false, new Random(), 0, );
                renderBuffer.getBuffer(RenderType.cutout()).putBulkData(matrixStack.last(), new FaithPlateTargetBakedModel().getQuads(null, hitDirection, null, null).get(0), 1, 1, 1, combinedLight, combinedOverlay);

//                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), renderBuffer.getBuffer(RenderTypeLookup.getRenderType(p_228791_1_, false)), p_228791_1_, ibakedmodel, f, f1, f2, p_228791_4_, p_228791_5_, modelData);

//                Minecraft.getInstance().getBlockRenderer().renderBlock(
//                        Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, hitDirection),
//                        matrixStack, renderBuffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
                return;
            }

//            if(!state.isAir() && state != thisState) {
//                matrixStack.popPose();
//                BlockPos offset2 = pos.subtract(thisPos).relative(hitDirection);
//                matrixStack.translate(offset2.getX(), offset2.getY(), offset2.getZ());
//                Minecraft.getInstance().getBlockRenderer().renderBlock(
//                        Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, hitDirection),
//                        matrixStack, renderBuffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
//                return;
//            }

            if(tileEntity.getBlockPos().getY() + y < -100)
                break;

            final float currentDistance = (float)Math.sqrt(x * x + z * z);
//            final int rate = 3;
//            final float scaledRate = distance / (float)rate;
//            if((int)currentDistance % Math.ceil(scaledRate) > scaledRate / 2f)
//                continue;

            if((int)currentDistance % 2 != 0)
                continue;

            vertexBuilder.vertex(matrix4f, x, y, z).color(1, 1, 1, 1f).endVertex();

            float i2 = i + .1f;
            float x2 = (float)(i2 * parabola.getComponentX());
            float z2 = (float)(i2 * parabola.getComponentZ());

//            float x2 = x + .1f;
            float y2 = (float)(a * i2 * i2 + b * i2);
            vertexBuilder.vertex(matrix4f, x2, y2, z2).color(1, 1, 1, 1f).endVertex();
        }

        matrixStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FaithPlateTileEntity tileEntity) {
        return true;
    }
}