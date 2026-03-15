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
import net.minecraft.world.World;
import net.portalmod.core.injectors.LivingEntityInjector;

import java.util.UUID;

public class PropulsionGelBlock extends AbstractGelBlock {
    private static final UUID SPEED_MODIFIER_GEL = UUID.fromString("46ea8b5a-6e03-44d0-b9b3-ed94341b0c51");
    private static final UUID SPEED_MODIFIER_GEL_BOUNCE = UUID.fromString("69ea0d28-0fc5-4cfd-8640-1525a61295cc");

    private static AttributeModifier gelSpeedBoost = new AttributeModifier(SPEED_MODIFIER_GEL,
                "Propulsion Gel Boost", .1F, AttributeModifier.Operation.ADDITION);
    private static AttributeModifier gelSpeedBounceBoost = new AttributeModifier(SPEED_MODIFIER_GEL_BOUNCE,
                "Propulsion Gel Bounce Boost", .6F, AttributeModifier.Operation.ADDITION);
            // Only applied when bouncing from Propulsion Gel

    public PropulsionGelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
//        double slipperiness  = 1.05;
//        if (shouldGetGelBoost(pos, state, entity)) {
//            Vector3d motion = entity.getDeltaMovement();
//            entity.setDeltaMovement(motion.multiply(slipperiness, 1, slipperiness));
//        }
        super.entityInside(state, level, pos, entity);
    }

    public static boolean isOnGel(BlockPos pos, BlockState state, Entity entity) {
        VoxelShape shapeEntity = state.getShape(entity.level, pos, ISelectionContext.of(entity));
        VoxelShape alignedShapeEntity = shapeEntity.move(pos.getX(), pos.getY(), pos.getZ());

        return VoxelShapes.joinIsNotEmpty(alignedShapeEntity,
                VoxelShapes.create(entity.getBoundingBox().inflate(.001f)), IBooleanFunction.AND);
    }

    public static void applyPropulsionSpeedBoost(LivingEntity entity, int propulsionTicks) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        double speedBoost = .1F * (1 + Math.cos(Math.PI + Math.PI * propulsionTicks / IGelAffected.MAX_PROPULSION_TICKS)) / 2;

        AttributeModifier propulsionGelBoost = new AttributeModifier(SPEED_MODIFIER_GEL,
                "Propulsion Gel Boost", speedBoost, AttributeModifier.Operation.ADDITION);

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL) == null) speedAttribute.addTransientModifier(propulsionGelBoost);
    }

    public static void applyPropulsionBounceBoost(LivingEntity entity) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        AttributeModifier gelSpeedBounceBoost = new AttributeModifier(SPEED_MODIFIER_GEL_BOUNCE,
                "Propulsion Gel Bounce Boost", .6F, AttributeModifier.Operation.ADDITION);

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL_BOUNCE) == null) speedAttribute.addTransientModifier(gelSpeedBounceBoost);
    }

    public static void removePropulsionSpeedBoost(LivingEntity entity) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(SPEED_MODIFIER_GEL);
    }

    public static void removePropulsionBounceBoost(LivingEntity entity) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(SPEED_MODIFIER_GEL_BOUNCE);
    }

    public static void onPreTick(LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level.getBlockState(pos);
        boolean isPropulsionGel = state.getBlock() instanceof PropulsionGelBlock;

        IGelAffected gelAffected = ((IGelAffected) entity);

        if (!isPropulsionGel && LivingEntityInjector.effectsShouldBeReset(entity, true)) {
            removePropulsionBounceBoost(entity);
        }

        if (isPropulsionGel && isOnGel(pos, state, entity)) {
            gelAffected.incrementPropulsionTicks();
        } else {
            gelAffected.decrementPropulsionTicks();
        }

        if (gelAffected.getPropulsionTicks() > 0) {
            removePropulsionSpeedBoost(entity);
            applyPropulsionSpeedBoost(entity, gelAffected.getPropulsionTicks());

            if (gelAffected.getBounced()) {
                applyPropulsionBounceBoost(entity);
            }
        } else {
            removePropulsionSpeedBoost(entity);
        }
    }

}