package net.portalmod.common.sorted.cube.storage;

import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeModel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class StorageCubeGlowLayer<T extends Cube> extends RenderLayer<T, CubeModel<T>> {
    private static final RenderType STORAGE_CUBE_GLOW = RenderType.entityCutout(new ResourceLocation(PortalMod.MODID, "textures/entity/cube/storage_cube_glow.png"));
    private static final RenderType STORAGE_CUBE_ACTIVE_GLOW = RenderType.entityCutout(new ResourceLocation(PortalMod.MODID, "textures/entity/cube/storage_cube_active_glow.png"));

    public StorageCubeGlowLayer(RenderLayerParent<T, CubeModel<T>> p_i50939_1_) {
        super(p_i50939_1_);
    }

    public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int i, T cube, float f1, float f2, float f3, float f4, float f5, float f6) {
        VertexConsumer ivertexbuilder = renderTypeBuffer.getBuffer(cube.isActive() ? STORAGE_CUBE_ACTIVE_GLOW : STORAGE_CUBE_GLOW);
        this.getParentModel().renderToBuffer(matrixStack, ivertexbuilder, LightTexture.pack(
                Math.max(LightTexture.block(i), 9),
                Math.max(LightTexture.sky(i), 9)
        ), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}