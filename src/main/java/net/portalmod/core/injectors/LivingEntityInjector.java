package net.portalmod.core.injectors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.gel.IGelAffected;
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.math.AABBUtil;

import java.util.List;

public class LivingEntityInjector {
    public static void onPreTick(LivingEntity entity) {
        RepulsionGelBlock.onPreTick(entity);
        PropulsionGelBlock.onPreTick(entity);
        if (effectsShouldBeReset(entity, true) && entity.getDeltaMovement().y < 0) ((Flingable) entity).setFlinging(false);
    }

    public static void onPostTick(LivingEntity entity) {
        RepulsionGelBlock.onPostTick(entity);
        IGelAffected gelAffected = (IGelAffected) entity;
        gelAffected.setWasOnGround(entity.isOnGround());
        gelAffected.setLastLastDeltaMovement(gelAffected.getLastDeltaMovement());
        gelAffected.setLastDeltaMovement(entity.getDeltaMovement());
        gelAffected.setHorizontalBounced(false);
    }

    public static boolean effectsShouldBeReset(LivingEntity entity, boolean includeOnGround) {
        AxisAlignedBB aabb = entity.getBoundingBox().deflate(0.001);
        List<BlockPos> blockPoses = AABBUtil.getBlocksWithin(aabb);
        boolean isInGoo = false;

        for(BlockPos pos : blockPoses) {
            FluidState fluidstate = entity.level.getFluidState(pos);
            if(!fluidstate.is(FluidTagInit.GOO))
                continue;

            float height = pos.getY() + fluidstate.getHeight(entity.level, pos);
            if(height >= aabb.minY) {
                isInGoo = true;
                break;
            }
        }

        return (
            (includeOnGround && entity.isOnGround()) ||
            entity.isInWall() ||
            entity.isInWater() ||
            entity.isInLava() ||
            isInGoo ||
            entity.isNoGravity() ||
            entity.hasEffect(Effects.LEVITATION) ||
            (entity instanceof PlayerEntity &&
                ((PlayerEntity) entity).abilities.flying
            )
        );
    }
}