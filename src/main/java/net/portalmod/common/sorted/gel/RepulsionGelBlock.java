package net.portalmod.common.sorted.gel;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import static net.portalmod.common.sorted.faithplate.FaithPlateParabola.GRAVITY;

public class RepulsionGelBlock extends AbstractGelBlock {
    public static float minBounceHeight = 4; // blocks

    @Override
    public void fallOn(World world, BlockPos blockPos, Entity entity, float fallDistance) {
        if (!entity.isShiftKeyDown()) {
            super.fallOn(world, blockPos, entity, fallDistance); // Negate fall damage unless shifting
        }
    }

    public RepulsionGelBlock(Properties properties) {
        super(properties);
    }

    public static void verticalBounce(Entity entity, float velocity) {
        IGelAffected gelAffected = (((IGelAffected) entity));
        if (gelAffected.getBounced()) return;
        gelAffected.setBounced(true);

        playBounceSound(entity);
        entity.setDeltaMovement(entity.getDeltaMovement().x, velocity, entity.getDeltaMovement().z);

        // Reset some variables
        entity.fallDistance = 0;
        gelAffected.setLastNeurtalHeight(0);
    }

    public static void playBounceSound(Entity entity) {
        entity.playSound(SoundInit.REPULSION_GEL_BOUNCE.get(), 1, ModUtil.randomSoundPitch());
    }

    public static double checkSpeedInDirection(Entity entity, Direction direction) {
        Vector3d deltaMovement = ((IGelAffected) entity).getLastDeltaMovement();
        switch (direction) {
            case NORTH: return -deltaMovement.z;
            case EAST:  return  deltaMovement.x;
            case SOUTH: return  deltaMovement.z;
            case WEST:  return -deltaMovement.x;
            default: return 0;
        }
    }

    public static void horizontalBounce(BlockPos pos, Entity entity, Direction direction) {
        Vector3d deltaMovement = ((IGelAffected)entity).getLastDeltaMovement();

        if (entity.isSpectator() || ((IGelAffected) entity).getHorizontalBounced()) return;

        float horizontalBounceAmount = 0.7F;
        float verticalBounceAmount = 0.25F;
        float speedBounceBonusAmount = 1.4F;
        float maxSpeedBoostAmount = 2.0F;

        Vector3d bounceDir = new Vector3d(direction.getOpposite().getStepX(), 0, direction.getOpposite().getStepZ());
        Vector3d parallel = new Vector3d(Math.abs(bounceDir.z), 0, Math.abs(bounceDir.x));

        Vector3d bounceSurface = Vector3d.atCenterOf(pos).add(bounceDir.scale(-.5F));
        Vector3d distance = entity.getPosition(1F).subtract(bounceSurface);

        // Check whether the player is nearby enough
        if (direction.getAxis() == Direction.Axis.X) {
            if (Math.abs(distance.x) > entity.getBbWidth() / 1.5F) return;
        } else {
            if (Math.abs(distance.z) > entity.getBbWidth() / 1.5F) return;
        }

        float speed = (float) checkSpeedInDirection(entity, direction);
        if (speed > 0.1 || (Minecraft.getInstance().options.keyJump.isDown())) {

            // This also applies a boost along the plane of the gel
            speedBounceBonusAmount = (deltaMovement.multiply(parallel).length() < maxSpeedBoostAmount) ? speedBounceBonusAmount : 1.0F;

            entity.setDeltaMovement(bounceDir.scale(horizontalBounceAmount)
                    .add(0, Math.max(verticalBounceAmount, entity.getDeltaMovement().y), 0)
                    .add(deltaMovement.multiply(parallel).scale(speedBounceBonusAmount))
            );

            entity.playSound(SoundInit.REPULSION_GEL_BOUNCE.get(), 1, ModUtil.randomSoundPitch());

            // horizontalBounced lasts for one tick, to make sure only one bounce happens
            ((IGelAffected) entity).setHorizontalBounced(true);
        }
    }

    public static void calculateBounce(World level, BlockState state, BlockPos pos, Entity entity) {
        IGelAffected gelAffected = ((IGelAffected) entity);
        float playerFallHeight = (float) Math.floor(gelAffected.getLastNeutralHeight() - pos.getY());

        if (playerFallHeight >= 193) {
            playerFallHeight = (float) (Math.pow(playerFallHeight - 193, 2F/3F) + 192);
        }

        boolean bounceVertical = state.getValue(DOWN);

        if (!entity.isShiftKeyDown()) {
            if (state.getValue(NORTH)) horizontalBounce(pos, entity, Direction.NORTH);
            if (state.getValue(EAST))  horizontalBounce(pos, entity, Direction.EAST);
            if (state.getValue(SOUTH)) horizontalBounce(pos, entity, Direction.SOUTH);
            if (state.getValue(WEST))  horizontalBounce(pos, entity, Direction.WEST);
        }

        boolean bounceFromAbove = playerFallHeight > 0.1 && entity.isOnGround();
        boolean bounceFromSpeed = entity.getDeltaMovement().length() > 0.2 && entity.isOnGround();
        boolean bounceFromJump = gelAffected.getWasOnGround() && entity.getDeltaMovement().y > 0.2;

        double heightInBlock = entity.position().y % 1;
        if (heightInBlock > 0.5) return; // Fix (#51)

        // Vertical bounce
        if (state.getBlock() == BlockInit.REPULSION_GEL.get() && bounceVertical &&
                (!entity.isShiftKeyDown() && (bounceFromAbove || bounceFromSpeed || bounceFromJump))) {

            entity.fallDistance = 0;

            if (entity instanceof PlayerEntity && !entity.level.isClientSide && playerFallHeight >= 100) {
                CriteriaTriggerInit.BOUNCE_ON_GEL.get().trigger((ServerPlayerEntity) entity); // Leap of Faith advancement
            }

            float x = Math.max(playerFallHeight, minBounceHeight);
//            float velocity = 1.028F * (float) (0.0173 * x + 0.286 * Math.pow(x, 1F/2F) + 0.13 * Math.pow(x, 1F/3F));
            float velocity = (float) (0.0178F * x + 0.294F * Math.pow(x, 1F/2F) + 0.134F * Math.pow(x, 1F/3F));
            // This function is the result of fine-tuning an approximation of the inverse of Minecraft's gravity and drag
            // calculations. See https://www.geogebra.org/calculator/qkdz2b9x.

            // If air friction is turned off, use a different bounce speed (#83)
            if (entity instanceof Flingable) {
                boolean launched = ((Flingable) entity).isFlinging();
                if (launched) velocity = (float) Math.sqrt(2 * GRAVITY * x);
            }

            verticalBounce(entity, velocity);

            if(level.isClientSide && (bounceFromSpeed || bounceFromJump)) {
                PacketInit.INSTANCE.sendToServer(new CRepulsionGelBouncePacket(true));
            }
        }
    }

    public static void onPreTick(LivingEntity entity) {
        IGelAffected gelAffected = ((IGelAffected) entity);
        if (entity.getDeltaMovement().y < -0.1) {
            if(entity.level.isClientSide) {
                gelAffected.setBounced(false);
                PacketInit.INSTANCE.sendToServer(new CRepulsionGelBouncePacket(false));
            }
        } else {
            gelAffected.setLastNeurtalHeight((float) entity.getY());
        }
    }

    public static void onPostTick(LivingEntity entity) {
        World level = entity.level;
        BlockPos pos = new BlockPos(entity.position());

        BlockState state = level.getBlockState(pos);
        BlockState stateAbove = level.getBlockState(pos.above());

        if (state.getBlock() == BlockInit.REPULSION_GEL.get()) {
            calculateBounce(level, state, pos, entity);
        }
        if (stateAbove.getBlock() == BlockInit.REPULSION_GEL.get()) {
            calculateBounce(level, stateAbove, pos.above(), entity);
        }
    }
}