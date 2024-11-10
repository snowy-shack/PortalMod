package net.portalmod.core.injectors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import net.portalmod.common.sorted.gel.IGelAffected;
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;

public class LivingEntityInjector {
    public static void onPreTick(LivingEntity entity) {
        RepulsionGelBlock.onPreTick(entity);
        PropulsionGelBlock.onPreTick(entity);
        if (effectsShouldBeReset(entity, true) && entity.getDeltaMovement().y < 0) ((IFaithPlateLaunchable) entity).setLaunched(false);

        if (effectsShouldBeReset(entity, false)) {
            ((IGelAffected) entity).setAffectedBySpeedGel(false);
        }
    }
    public static void onPostTick(LivingEntity entity) {
        RepulsionGelBlock.onPostTick(entity);
        ((IGelAffected) entity).setWasOnGround(entity.isOnGround());
        ((IGelAffected) entity).setLastDeltaMovement(entity.getDeltaMovement());
    }

    public static boolean effectsShouldBeReset(LivingEntity entity, boolean includeOnGround) {
        return (
            (includeOnGround && entity.isOnGround()) ||
            entity.isInWall() ||
            entity.isInWater() ||
            entity.isInLava() ||
            entity.isNoGravity() ||
            entity.hasEffect(Effects.LEVITATION) ||
            (entity instanceof PlayerEntity &&
                ((PlayerEntity) entity).abilities.flying
            )
        );
    }
}