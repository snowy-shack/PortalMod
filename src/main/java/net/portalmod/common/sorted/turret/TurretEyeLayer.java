package net.portalmod.common.sorted.turret;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.portalmod.PortalMod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TurretEyeLayer<T extends TurretEntity> extends EyesLayer<T, TurretModel<T>> {
    private static final RenderType TURRET_EYE = RenderType.eyes(new ResourceLocation(PortalMod.MODID, "textures/entity/turret/turret_eye.png"));

    public TurretEyeLayer(RenderLayerParent<T, TurretModel<T>> p_i50928_1_) {
        super(p_i50928_1_);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int p_225628_3_, T turretEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (turretEntity.getState() != TurretState.DEAD) {
            super.render(matrixStack, iRenderTypeBuffer, p_225628_3_, turretEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    public RenderType renderType() {
        return TURRET_EYE;
    }
}