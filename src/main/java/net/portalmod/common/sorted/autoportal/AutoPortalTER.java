package net.portalmod.common.sorted.autoportal;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.PortalMod;
import net.portalmod.client.render.DynamicTextureVertexBuilder;
import net.portalmod.common.sorted.button.QuadBlockCorner;
import net.portalmod.common.sorted.portal.OrthonormalBasis;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import java.util.Optional;

public class AutoPortalTER extends TileEntityRenderer<AutoPortalTileEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "entity/autoportal");
    public static RenderMaterial MATERIAL;
    private final AutoPortalModel model;

    public AutoPortalTER(TileEntityRendererDispatcher terd) {
        super(terd);
        this.model = new AutoPortalModel();
        MATERIAL = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, TEXTURE);
    }

    @Override
    public void render(AutoPortalTileEntity be, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light, int overlay) {
        if(be.getBlockState().getBlock() instanceof AutoPortalBlock) {
            AutoPortalBlock block = (AutoPortalBlock)be.getBlockState().getBlock();
            Direction facing = be.getBlockState().getValue(AutoPortalBlock.FACING);
            Direction direction = be.getBlockState().getValue(AutoPortalBlock.DIRECTION);
            BlockPos pos = be.getBlockPos();

            Vec3 up = new Vec3(block.getOtherBlock(pos, QuadBlockCorner.DOWN_LEFT, QuadBlockCorner.UP_LEFT, facing, direction).subtract(pos));
            Vec3 right = new Vec3(block.getOtherBlock(pos, QuadBlockCorner.DOWN_LEFT, QuadBlockCorner.DOWN_RIGHT, facing, direction).subtract(pos));
            Mat4 matrix = new OrthonormalBasis(right, up).getChangeOfBasisFromCanonicalMatrix();

            up = up.mul(.5);
            right = right.mul(.5);
            Vec3 normal = new Vec3(facing).mul(-.499);

            matrixStack.pushPose();
            matrixStack.translate(right.x, right.y, right.z);
            matrixStack.translate(up.x, up.y, up.z);
            matrixStack.translate(normal.x, normal.y, normal.z);
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.last().pose().multiply(matrix.to4f());
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
            matrixStack.translate(0, -1.5, 0);

            IVertexBuilder vertexBuilder = MATERIAL.buffer(renderBuffer, RenderType::entityTranslucent);
            DynamicTextureVertexBuilder dtvb = new DynamicTextureVertexBuilder(vertexBuilder);

            model.frame.render(matrixStack, dtvb, light, overlay, 1, 1, 1, 1);

            Optional<Integer> colorIndex = be.getCurrentColorIndex();
            if(colorIndex.isPresent()) {
                int color = colorIndex.get();
                float u = (color % 4) * 11 / 64f;
                float v = (float) (color / 4) * 16 / 64f;
                dtvb.setOffset(u, v);
                model.wawas.render(matrixStack, dtvb, LightTexture.pack(15, 15), overlay, 1, 1, 1, 1);
            }

            matrixStack.popPose();
        }
    }
}