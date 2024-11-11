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
import net.portalmod.core.init.BlockInit;
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

    public static boolean shouldGetGelBoost(BlockPos pos, BlockState state, Entity entity) {
        VoxelShape shapeEntity = state.getCollisionShape(entity.level, pos, ISelectionContext.of(entity));
        VoxelShape alignedShapeEntity = shapeEntity.move(pos.getX(), pos.getY(), pos.getZ());

        return VoxelShapes.joinIsNotEmpty(alignedShapeEntity,
                VoxelShapes.create(entity.getBoundingBox().inflate(.001f)), IBooleanFunction.AND);
    }

    public static void applyGelSpeedBoost(LivingEntity entity, boolean bounced) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL) == null) speedAttribute.addTransientModifier(gelSpeedBoost);
        if (speedAttribute.getModifier(SPEED_MODIFIER_GEL_BOUNCE) == null && bounced) speedAttribute.addTransientModifier(gelSpeedBounceBoost);
    }

    public static void removeGelSpeedBoost(LivingEntity entity, Boolean onlyBounceBoost) {
        ModifiableAttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        speedAttribute.removeModifier(gelSpeedBounceBoost);
        if (onlyBounceBoost) return;

        speedAttribute.removeModifier(gelSpeedBoost);
    }

    public static void onPreTick(LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level.getBlockState(pos);

        IGelAffected gelAffected = ((IGelAffected) entity);

        if (LivingEntityInjector.effectsShouldBeReset(entity, true)
                && state.getBlock() != BlockInit.REPULSION_GEL.get()) removeGelSpeedBoost(entity, true);

        if (state.getBlock() == BlockInit.PROPULSION_GEL.get() && (shouldGetGelBoost(pos, state, entity))) {
            gelAffected.setTicksSinceSpeedGel(0);
            gelAffected.setAffectedBySpeedGel(true);

        } else if (LivingEntityInjector.effectsShouldBeReset(entity, true)
                && !(state.getBlock() == BlockInit.REPULSION_GEL.get() && !entity.isShiftKeyDown())) {
            gelAffected.setTicksSinceSpeedGel(Math.min(gelAffected.getTicksSinceSpeedGel() + 1, 20));
        }

        if (gelAffected.getTicksSinceSpeedGel() < 2 && !entity.isOnGround()) gelAffected.setTicksSinceSpeedGel(2); // So that you can't keep the boost by bhopping
        if (gelAffected.getTicksSinceSpeedGel() > 2) gelAffected.setAffectedBySpeedGel(false);

        if (gelAffected.getAffectedBySpeedGel()) {
            applyGelSpeedBoost(entity, gelAffected.getBounced());
        } else {
            removeGelSpeedBoost(entity, false);
        }
    }

}