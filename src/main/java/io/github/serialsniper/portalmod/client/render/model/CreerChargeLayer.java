package io.github.serialsniper.portalmod.client.render.model;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EnergyLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;

public class CreerChargeLayer extends EnergyLayer<CreeperEntity, CreerModel<CreeperEntity>> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final CreerModel<CreeperEntity> model = new CreerModel<>(2.0F);

    public CreerChargeLayer(IEntityRenderer<CreeperEntity, CreerModel<CreeperEntity>> p_i50947_1_) {
        super(p_i50947_1_);
    }

    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.01F;
    }

    protected ResourceLocation getTextureLocation() {
        return POWER_LOCATION;
    }

    protected EntityModel<CreeperEntity> model() {
        return this.model;
    }
}