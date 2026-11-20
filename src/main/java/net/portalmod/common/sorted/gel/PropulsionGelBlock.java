package net.portalmod.common.sorted.gel;

import net.portalmod.core.init.PacketInit;
import net.portalmod.core.injectors.LivingEntityInjector;

import java.util.UUID;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;

public class PropulsionGelBlock extends AbstractGelBlock {
    private static final UUID SPEED_MODIFIER_GEL = UUID.fromString("46ea8b5a-6e03-44d0-b9b3-ed94341b0c51");
    private static final UUID SPEED_MODIFIER_GEL_BOUNCE = UUID.fromString("69ea0d28-0fc5-4cfd-8640-1525a61295cc");

    public PropulsionGelBlock(Properties properties) {
        super(properties);
    }

    public static boolean actuallyOnSpeedGel(BlockPos pos, BlockState state, Entity entity) {
        VoxelShape shapeEntity = state.getShape(entity.level(), pos, CollisionContext.of(entity));
        VoxelShape alignedShapeEntity = shapeEntity.move(pos.getX(), pos.getY(), pos.getZ());

        return Shapes.joinIsNotEmpty(alignedShapeEntity,
                Shapes.create(entity.getBoundingBox().inflate(.001f)), BooleanOp.AND);
    }

    public static void applyGelSpeedBoost(LivingEntity entity, int propulsionTicks) {
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        double speedBoost = .12F * (1 + Math.cos(Math.PI + Math.PI * propulsionTicks / IGelAffected.MAX_PROPULSION_TICKS)) / 2;

        AttributeModifier propulsionGelBoost = new AttributeModifier(SPEED_MODIFIER_GEL,
                "Propulsion Gel Boost", speedBoost, AttributeModifier.Operation.ADDITION);

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL) == null) speedAttribute.addTransientModifier(propulsionGelBoost);
    }

    public static void applyBounceSpeedBoost(LivingEntity entity) {
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        AttributeModifier gelSpeedBounceBoost = new AttributeModifier(SPEED_MODIFIER_GEL_BOUNCE,
                "Propulsion Gel Bounce Boost", .7F, AttributeModifier.Operation.ADDITION);

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL_BOUNCE) == null) speedAttribute.addTransientModifier(gelSpeedBounceBoost);
    }

    public static void removeGelSpeedBoost(LivingEntity entity) {
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(SPEED_MODIFIER_GEL);
    }

    public static void removeBounceSpeedBoost(LivingEntity entity) {
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(SPEED_MODIFIER_GEL_BOUNCE);
    }

    public static void onPreTick(LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level().getBlockState(pos);
        boolean isInSpeedGel = state.getBlock() instanceof PropulsionGelBlock;

        IGelAffected gelAffected = ((IGelAffected) entity);

        // to each their own
        if (entity.level().isClientSide == entity instanceof Player) {
            if (isInSpeedGel) {
                if (actuallyOnSpeedGel(pos, state, entity)) {
                    double speed = entity.getDeltaMovement().multiply(1, 0, 1).length();

                    if (speed > 0.01) {
                        gelAffected.incrementPropulsionTicks();
                    } else {
                        gelAffected.decrementPropulsionTicks();
                    }

                    Vec3 velocity = entity.getDeltaMovement().multiply(1, 0, 1).normalize();
                    Vec3 oldVelocity = ((IGelAffected) entity).getLastLastDeltaMovement().multiply(1, 0, 1).normalize();
                    gelAffected.setPropulsionTicks((int)Math.round(gelAffected.getPropulsionTicks() * (velocity.dot(oldVelocity) / 2 + 0.5)));
                }
            } else {
                // Preserve speed while jumping / bouncing
                if (entity.onGround() && gelAffected.getLeftGround() && !(state.getBlock() instanceof RepulsionGelBlock)) {
                    gelAffected.setPropulsionTicks(0);
                }

                if (LivingEntityInjector.effectsShouldBeReset(entity, true)) {
                    gelAffected.decrementPropulsionTicks();
                }
            }

            if (entity.level().isClientSide) {
                PacketInit.INSTANCE.sendToServer(new CPropulsionGelBoostTickPacket(gelAffected.getPropulsionTicks()));
            }
        }

        removeGelSpeedBoost(entity);
        applyGelSpeedBoost(entity, gelAffected.getPropulsionTicks());

        if (gelAffected.getPropulsionTicks() > 0) {
            if (gelAffected.getBounced()) {
                applyBounceSpeedBoost(entity);
            } else {
                removeBounceSpeedBoost(entity);
            }
        } else {
            removeGelSpeedBoost(entity);
        }

        gelAffected.setLeftGround(!entity.onGround());
    }

}