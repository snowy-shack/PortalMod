package net.portalmod.common.blocks;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link MultiBlock} for two-high blocks
 */
public class DoubleBlock extends MultiBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public DoubleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        return new ArrayList<>(Collections.singletonList(mainPos.above()));
    }

    @Override
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        world.setBlockAndUpdate(pos.relative(isLower ? Direction.UP : Direction.DOWN), blockState.setValue(HALF, isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER));
    }

    @Override
    public StatePropertiesPredicate.Builder mainBlockPredicate() {
        return StatePropertiesPredicate.Builder.properties().hasProperty(HALF, DoubleBlockHalf.LOWER);
    }

    protected Boolean shouldBeTopHalf(BlockItemUseContext context, Direction.Axis axis) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean placedOnLowerSide = false;
        if (axis == Direction.Axis.X) placedOnLowerSide = context.getClickLocation().x - pos.getX() < 0.5;
        if (axis == Direction.Axis.Y) placedOnLowerSide = context.getClickLocation().y - pos.getY() < 0.5;
        if (axis == Direction.Axis.Z) placedOnLowerSide = context.getClickLocation().z - pos.getZ() < 0.5;

        boolean canBeLower = world.getBlockState(pos.relative(
                Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE))).canBeReplaced(context);
        boolean canBeUpper = world.getBlockState(pos.relative(
                Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE))).canBeReplaced(context);

        // Placement preference
        if (placedOnLowerSide && canBeUpper) return true;
        if (!placedOnLowerSide && canBeLower) return false;

        // Placement fallback
        if (canBeUpper) return true;
        if (canBeLower) return false;

        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Boolean half = shouldBeTopHalf(context, Direction.Axis.Y);
        if (half == null) return null;

        return this.defaultBlockState().setValue(
                HALF, half ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER
        );
    }
}
