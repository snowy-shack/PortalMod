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

public class LivingEntityInjector {
    public static float fallDistance;
    public static double previousfallDistance = 0;

    public static void onPreTick(LivingEntity entity) {
        if(entity instanceof PlayerEntity && entity.level.isClientSide)
            System.out.println(entity.position());
        fallDistance = (float)Math.ceil(entity.fallDistance + 1) + .1f; // why

//        if (entity.isOnGround()) {((IFaithPlateLaunchable)entity).setLaunched(false);}
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

        if(state.getBlock() == BlockInit.REPULSION_GEL.get() && entity.isOnGround() && !entity.isShiftKeyDown() && fallDistance > 0) {

            entity.level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundInit.GEL_BLOCK_BOUNCE.get(), SoundCategory.BLOCKS, 1, 1);

            if(entity instanceof PlayerEntity && !entity.level.isClientSide && previousfallDistance >= 1) {
                CriteriaTriggerInit.BOUNCE_ON_GEL.get().trigger((ServerPlayerEntity)entity);
            }
//            System.out.println(fallDistance);
            ((IFaithPlateLaunchable)entity).setLaunched(true);
            float velocity = (float)Math.sqrt(Math.max(fallDistance < 11 ? fallDistance - 1 : fallDistance, 5) * 1.8 * 0.08);
            entity.setDeltaMovement(entity.getDeltaMovement().x, velocity, entity.getDeltaMovement().z);
            entity.fallDistance = 0;
        }

        previousfallDistance = fallDistance;
    }
}