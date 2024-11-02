package net.portalmod.core.injectors;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

public class LivingEntityInjector {
    public static float fallDistance;   // TODO PHANTY THAT IS NOT SUPPOSED TO BE STATIC
    public static float minBounceHeight = 5;

    public static void onPreTick(LivingEntity entity) {
//        if(entity instanceof PlayerEntity && entity.level.isClientSide)
//            System.out.println(entity.position());
        fallDistance = (float)entity.fallDistance + 1 + .1f; // why
    }

    public static void onPostTick(LivingEntity entity) {
        World level = entity.level;
        BlockPos pos = new BlockPos(entity.position());
        BlockState state = level.getBlockState(pos);
        
        VoxelShape voxelshape = state.getCollisionShape(level, pos, ISelectionContext.of(entity));
        VoxelShape voxelshape1 = voxelshape.move(pos.getX(), pos.getY(), pos.getZ());
        boolean flag = VoxelShapes.joinIsNotEmpty(voxelshape1, VoxelShapes.create(entity.getBoundingBox().inflate(200f)), IBooleanFunction.AND);

//        if(entity instanceof PlayerEntity)
//            System.out.println(((IFaithPlateLaunchable)entity).isLaunched());

        if(state.getBlock() == BlockInit.REPULSION_GEL.get() && (entity.isOnGround() && !entity.isShiftKeyDown() && (fallDistance > 1.2 || entity.getDeltaMovement().length() > 0.2))) {

            if (entity instanceof PlayerEntity && !entity.level.isClientSide && fallDistance >= 100) {
                CriteriaTriggerInit.BOUNCE_ON_GEL.get().trigger((ServerPlayerEntity) entity);
            }
            ((IFaithPlateLaunchable) entity).setLaunched(true);

            entity.level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundInit.REPULSION_GEL_BOUNCE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSoundPitch());
            float velocity = (float) Math.sqrt(Math.max(fallDistance < 11 ? fallDistance - 1 : fallDistance, minBounceHeight) * 1.8 * 0.08); // for some reason it's broken if fallDistance >= 11.1, this is a bad temporary fix sorry
            entity.setDeltaMovement(entity.getDeltaMovement().x, velocity, entity.getDeltaMovement().z);
            entity.fallDistance = 0;
        }
    }
}