package net.portalmod.common.sorted.gel;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

public class RepulsionGelBlock extends AbstractGelBlock {
    public static float minBounceHeight = 5;

    @Override
    public void fallOn(World world, BlockPos blockPos, Entity entity, float fallDistance) {
//        super.fallOn(world, blockPos, entity, fallDistance);
    }

    public RepulsionGelBlock(Properties properties) {
        super(properties);
    }

    public static void bounce(Entity entity, float velocity) {
        if (((IGelBouncable) entity).getBounced()) return;
        ((IGelBouncable) entity).setBounced(true);

        ModUtil.sendChat(entity.level, ((IGelBouncable) entity).getLastFallDistance());
        entity.level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundInit.REPULSION_GEL_BOUNCE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSoundPitch());
        entity.setDeltaMovement(entity.getDeltaMovement().x, velocity, entity.getDeltaMovement().z);
    }

    public static void calculateBounce(World level, BlockState state, BlockPos pos, Entity entity) {
        IGelBouncable gelBouncable = ((IGelBouncable) entity);
        float prevFallDistance = (float) gelBouncable.getLastFallDistance(); // Add math.floor

        boolean bounceVertical = state.getValue(DOWN);

//        VoxelShape voxelshape = state.getCollisionShape(level, pos, ISelectionContext.of(entity));
//        VoxelShape voxelshape1 = voxelshape.move(pos.getX(), pos.getY(), pos.getZ());
//        boolean flag = VoxelShapes.joinIsNotEmpty(voxelshape1, VoxelShapes.create(entity.getBoundingBox().inflate(200f)), IBooleanFunction.AND);

        boolean bounceFromAbove = prevFallDistance > 0.4 && entity.isOnGround();
        boolean bounceFromSpeed = entity.getDeltaMovement().length() > 0.2 && entity.isOnGround();
        boolean bounceFromJump = gelBouncable.getWasOnGround() && entity.getDeltaMovement().y > 0.2;

        if (entity.getDeltaMovement().y > 0) {
//            ModUtil.sendChat(level, gelBouncable.getWasOnGround());
//            ModUtil.sendChat(level, entity.getDeltaMovement().y);
        }

        // Vertical bounce
        if (state.getBlock() == BlockInit.REPULSION_GEL.get() && bounceVertical &&
                (!entity.isShiftKeyDown() && (bounceFromAbove || bounceFromSpeed || bounceFromJump))) {

            if (entity instanceof PlayerEntity && !entity.level.isClientSide && prevFallDistance >= 100) {
                CriteriaTriggerInit.BOUNCE_ON_GEL.get().trigger((ServerPlayerEntity) entity); // Leap of Faith advancement
            }

//            ((IFaithPlateLaunchable) entity).setLaunched(true);

//            float velocity = (float) Math.sqrt(Math.max(prevFallDistance, minBounceHeight) * 1.8 * 0.08); // for some reason it's broken if fallDistance >= 11.1, this is a bad temporary fix sorry
            float bounceBlocks = Math.max(prevFallDistance, minBounceHeight);
            float velocity = (float) (-0.1F +
                    0.5F * Math.sqrt(bounceBlocks) +
                    0.0006F * bounceBlocks * Math.sqrt(bounceBlocks));

            bounce(entity, velocity);
        }
    }

    public static void onPreTick(LivingEntity entity) {
        ((IGelBouncable) entity).setLastFallDistance(entity.fallDistance);
        if (entity.getDeltaMovement().y < 0) ((IGelBouncable) entity).setBounced(false);
    }

    public static void onPostTick(LivingEntity entity) {
        World level = entity.level;
        BlockPos pos = new BlockPos(entity.position());
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() == BlockInit.REPULSION_GEL.get() && !((IGelBouncable) entity).getBounced()) {
            calculateBounce(level, state, pos, entity);
        };
        ((IGelBouncable) entity).setWasOnGround(entity.isOnGround());
    }
}