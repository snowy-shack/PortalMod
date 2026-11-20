package net.portalmod.common.sorted.creer;

import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.resources.ResourceLocation;

public class CreerChargeLayer extends EnergySwirlLayer<Creeper, CreerModel<Creeper>> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final CreerModel<Creeper> model = new CreerModel<>(2.0F);

    public CreerChargeLayer(RenderLayerParent<Creeper, CreerModel<Creeper>> p_i50947_1_) {
        super(p_i50947_1_);
    }

    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.01F;
    }

    protected ResourceLocation getTextureLocation() {
        return POWER_LOCATION;
    }

    protected EntityModel<Creeper> model() {
        return this.model;
    }
}