package net.portalmod.core.injectors;

import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.gel.IGelAffected;
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.math.AABBUtil;

import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

public class LivingEntityInjector {
    public static void onPreTick(LivingEntity entity) {
        RepulsionGelBlock.onPreTick(entity);
        PropulsionGelBlock.onPreTick(entity);
        if (effectsShouldBeReset(entity, true) && entity.getDeltaMovement().y < 0) ((Flingable) entity).setFlinging(false);
    }

    public static void onPostTick(LivingEntity entity) {
        RepulsionGelBlock.onPostTick(entity);
        IGelAffected gelAffected = (IGelAffected) entity;
        gelAffected.setWasOnGround(entity.onGround());
        gelAffected.setLastLastDeltaMovement(gelAffected.getLastDeltaMovement());
        gelAffected.setLastDeltaMovement(entity.getDeltaMovement());
        gelAffected.setHorizontalBounced(false);
    }

    public static boolean effectsShouldBeReset(LivingEntity entity, boolean includeOnGround) {
        AABB aabb = entity.getBoundingBox().deflate(0.001);
        List<BlockPos> blockPoses = AABBUtil.getBlocksWithin(aabb);
        boolean isInGoo = false;

        for(BlockPos pos : blockPoses) {
            FluidState fluidstate = entity.level().getFluidState(pos);
            if(!fluidstate.is(FluidTagInit.GOO))
                continue;

            float height = pos.getY() + fluidstate.getHeight(entity.level(), pos);
            if(height >= aabb.minY) {
                isInGoo = true;
                break;
            }
        }

        return (
            (includeOnGround && entity.onGround()) ||
            entity.isInWall() ||
            entity.isInWater() ||
            entity.isInLava() ||
            isInGoo ||
            entity.isNoGravity() ||
            entity.hasEffect(MobEffects.LEVITATION) ||
            (entity instanceof Player &&
                ((Player) entity).getAbilities().flying
            )
        );
    }
}