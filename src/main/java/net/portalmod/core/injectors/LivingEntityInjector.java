package net.portalmod.core.injectors;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.CriteriaTriggerInit;

public class LivingEntityInjector {
    public static float fallDistance;

    public static void onPreTick(LivingEntity entity) {
//        if(entity instanceof PlayerEntity && entity.level.isClientSide)
//            System.out.println(entity.position());
        fallDistance = (float)Math.ceil(entity.fallDistance + 1) + .1f;
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

        if(state.getBlock() == BlockInit.REPULSION_GEL.get() && entity.isOnGround()) {
//            entity.setDeltaMovement(0, 1, 0);
            if(entity instanceof PlayerEntity && !entity.level.isClientSide && fallDistance > 100) {
                CriteriaTriggerInit.BOUNCE_ON_GEL.get().trigger((ServerPlayerEntity)entity);
            }
//            System.out.println(fallDistance);
//            System.out.println("afwaWFAF");
            ((IFaithPlateLaunchable)entity).setLaunched(true);
            float velocity = (float)Math.sqrt(fallDistance * 1.8 * 0.08);
            entity.setDeltaMovement(entity.getDeltaMovement().x, velocity, entity.getDeltaMovement().z);
            entity.fallDistance = 0;
        }
    }
}