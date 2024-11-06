package net.portalmod.common.sorted.turret;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.AbstractEyesLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.portalmod.PortalMod;

@OnlyIn(Dist.CLIENT)
public class TurretEyeLayer<T extends TurretEntity> extends AbstractEyesLayer<T, TurretModel<T>> {
    private static final RenderType TURRET_EYE = RenderType.eyes(new ResourceLocation(PortalMod.MODID, "textures/entity/turret/turret_eye.png"));

    public TurretEyeLayer(IEntityRenderer<T, TurretModel<T>> p_i50928_1_) {
        super(p_i50928_1_);
    }

    public RenderType renderType() {
        return TURRET_EYE;
    }
}