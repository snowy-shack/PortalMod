package net.portalmod.common.sorted.gel;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.portalmod.core.injectors.LivingEntityInjector;

import java.util.UUID;

public class PropulsionGelBlock extends AbstractGelBlock {
    private static final UUID SPEED_MODIFIER_GEL = UUID.fromString("46ea8b5a-6e03-44d0-b9b3-ed94341b0c51");
    private static final UUID SPEED_MODIFIER_GEL_BOUNCE = UUID.fromString("69ea0d28-0fc5-4cfd-8640-1525a61295cc");

    public PropulsionGelBlock(Properties properties) {
        super(properties);
    }

    public static boolean actuallyOnSpeedGel(BlockPos pos, BlockState state, Entity entity) {
        VoxelShape shapeEntity = state.getShape(entity.level, pos, ISelectionContext.of(entity));
        VoxelShape alignedShapeEntity = shapeEntity.move(pos.getX(), pos.getY(), pos.getZ());

        return VoxelShapes.joinIsNotEmpty(alignedShapeEntity,
                VoxelShapes.create(entity.getBoundingBox().inflate(.001f)), IBooleanFunction.AND);
    }

    public static void applyGelSpeedBoost(LivingEntity entity, int propulsionTicks) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        double speedBoost = .12F * (1 + Math.cos(Math.PI + Math.PI * propulsionTicks / IGelAffected.MAX_PROPULSION_TICKS)) / 2;

        AttributeModifier propulsionGelBoost = new AttributeModifier(SPEED_MODIFIER_GEL,
                "Propulsion Gel Boost", speedBoost, AttributeModifier.Operation.ADDITION);

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL) == null) speedAttribute.addTransientModifier(propulsionGelBoost);
    }

    public static void applyBounceSpeedBoost(LivingEntity entity) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        AttributeModifier gelSpeedBounceBoost = new AttributeModifier(SPEED_MODIFIER_GEL_BOUNCE,
                "Propulsion Gel Bounce Boost", .7F, AttributeModifier.Operation.ADDITION);

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL_BOUNCE) == null) speedAttribute.addTransientModifier(gelSpeedBounceBoost);
    }

    public static void removeGelSpeedBoost(LivingEntity entity) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(SPEED_MODIFIER_GEL);
    }

    public static void removeBounceSpeedBoost(LivingEntity entity) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(SPEED_MODIFIER_GEL_BOUNCE);
    }

    public static void onPreTick(LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level.getBlockState(pos);
        boolean isInSpeedGel = state.getBlock() instanceof PropulsionGelBlock;

        IGelAffected gelAffected = ((IGelAffected) entity);

        if (isInSpeedGel) {
            if (actuallyOnSpeedGel(pos, state, entity)) {
                gelAffected.incrementPropulsionTicks();
            }
        } else {
            // Preserve speed while jumping / bouncing
            if (entity.isOnGround() && gelAffected.getLeftGround() && !(state.getBlock() instanceof RepulsionGelBlock)) {
                gelAffected.setPropulsionTicks(0);
            }

            if (LivingEntityInjector.effectsShouldBeReset(entity, true)) {
                gelAffected.decrementPropulsionTicks();
            }
        }

        if (gelAffected.getPropulsionTicks() >= 0) {
            removeGelSpeedBoost(entity);
            applyGelSpeedBoost(entity, gelAffected.getPropulsionTicks());

            if (gelAffected.getBounced()) {
                applyBounceSpeedBoost(entity);
            } else {
                removeBounceSpeedBoost(entity);
            }
        } else {
            removeGelSpeedBoost(entity);
        }

        gelAffected.setLeftGround(!entity.isOnGround());
    }

}