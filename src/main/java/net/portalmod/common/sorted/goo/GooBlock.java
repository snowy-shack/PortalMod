package net.portalmod.common.sorted.goo;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.init.ItemTagInit;
import net.portalmod.core.util.ModUtil;
import net.portalmod.mixins.accessors.EntityAccessor;

import java.util.function.Supplier;

public class GooBlock extends FlowingFluidBlock {
    public static final float FOG_DENSITY = 0.95f;
    public static final double MOVEMENT_FRICTION = 0.05;

    public GooBlock(Supplier<FlowingFluid> flowingFluid, Properties properties) {
        super(flowingFluid, properties);
    }

    public static void setFogColor(EntityViewRenderEvent.FogColors event) {
        event.setRed(70 / 256f);
        event.setGreen(53 / 256f);
        event.setBlue(29 / 256f);
    }

    public static void applyGooResistance(Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 0.6, 0.3));
    }

    public static boolean isInGoo(Entity entity) {
        return !((EntityAccessor) entity).pmGetFirstTick() && entity.getFluidHeight(FluidTagInit.GOO) > 0;
    }

    public static void addGooDamage(Entity entity) {
        entity.fallDistance = 0;

        float damage = 4;
        for (ItemStack itemStack : entity.getArmorSlots()) {
            if (itemStack.getItem().is(ItemTagInit.GOO_PROTECTION)) {
                damage = 0.2f;
                break;
            }
        }

        entity.hurt(FluidInit.GOO_DAMAGE, damage);
    }

    public static void handleGooDamage(LivingEntity entity, DamageSource damageSource) {
        entity.level.playSound(null, entity, entity.getHurtSound(damageSource), entity.getSoundSource(), 1, ModUtil.randomSoundPitch());
    }
}
