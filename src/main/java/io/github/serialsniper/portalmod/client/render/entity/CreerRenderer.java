package io.github.serialsniper.portalmod.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.serialsniper.portalmod.client.render.model.CreerChargeLayer;
import io.github.serialsniper.portalmod.client.render.model.CreerModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperChargeLayer;
import net.minecraft.client.renderer.entity.model.CreeperModel;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class CreerRenderer extends MobRenderer<CreeperEntity, CreerModel<CreeperEntity>> {
    private static final ResourceLocation CREEPER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper.png");

    public CreerRenderer(EntityRendererManager p_i46186_1_) {
        super(p_i46186_1_, new CreerModel<>(), 0.5F);
        this.addLayer(new CreerChargeLayer(this));
    }

    protected void scale(CreeperEntity p_225620_1_, MatrixStack p_225620_2_, float p_225620_3_) {
        float f = p_225620_1_.getSwelling(p_225620_3_);
        float f1 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.01F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = f * f;
        f = f * f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        p_225620_2_.scale(f2, f3, f2);
    }

    protected float getWhiteOverlayProgress(CreeperEntity p_225625_1_, float p_225625_2_) {
        float f = p_225625_1_.getSwelling(p_225625_2_);
        return (int)(f * 10.0F) % 2 == 0 ? 0.0F : MathHelper.clamp(f, 0.5F, 1.0F);
    }

    public ResourceLocation getTextureLocation(CreeperEntity p_110775_1_) {
        return CREEPER_LOCATION;
    }
}