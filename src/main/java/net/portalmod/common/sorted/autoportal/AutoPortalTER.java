package net.portalmod.common.sorted.autoportal;

import com.mojang.math.Axis;
import net.portalmod.PortalMod;
import net.portalmod.client.render.DynamicTextureVertexBuilder;
import net.portalmod.common.sorted.button.QuadBlockCorner;
import net.portalmod.common.sorted.portal.OrthonormalBasis;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

public class AutoPortalTER extends BlockEntityRenderer<AutoPortalTileEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "entity/autoportal");
    public static Material MATERIAL;
    private final AutoPortalModel model;

    public AutoPortalTER(BlockEntityRenderDispatcher terd) {
        super(terd);
        this.model = new AutoPortalModel();
        MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, TEXTURE);
    }

    @Override
    public void render(AutoPortalTileEntity be, float partialTicks, PoseStack matrixStack, MultiBufferSource renderBuffer, int light, int overlay) {
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
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
            matrixStack.translate(0, -1.5, 0);

            VertexConsumer vertexBuilder = MATERIAL.buffer(renderBuffer, RenderType::entityTranslucent);
            DynamicTextureVertexBuilder dtvb = new DynamicTextureVertexBuilder(vertexBuilder);

            int closedOffset = be.lastOpenedUUID != null ? 0 : 2;

            model.frame.render(matrixStack, dtvb, light, overlay, 1, 1, 1, 1);

            Optional<Integer> colorIndex = be.getCurrentColorIndex();
            if(colorIndex.isPresent()) {
                int color = colorIndex.get();
                float u = ((color % 4) * 11 + closedOffset) / 64f;
                float v = (float) (color / 4) * 16 / 64f;
                dtvb.setOffset(u, v);
                model.wawas.render(matrixStack, dtvb, LightTexture.pack(15, 15), overlay, 1, 1, 1, 1);
            }

            matrixStack.popPose();
        }
    }
}