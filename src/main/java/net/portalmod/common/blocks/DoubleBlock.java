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

import javax.annotation.Nullable;
import java.util.*;

/**
 * Implementation of {@link MultiBlock} for two-high blocks
 */
public abstract class DoubleBlock extends MultiBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public DoubleBlock(Properties properties) {
        super(properties);
    }

    /**
     * @return the {@link Direction} the upper block is in from the point of the lower block.
     */
    public abstract Direction getUpperDirection(BlockState state);

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.relative(this.getUpperDirection(blockState).getOpposite());
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        return new ArrayList<>(Collections.singletonList(mainPos.relative(this.getUpperDirection(blockState))));
    }

    @Override
    public Map<BlockPos, BlockState> getConnectedBlockStates(World world, BlockState blockState, BlockPos pos) {
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        Direction direction = this.getUpperDirection(blockState);

        HashMap<BlockPos, BlockState> map = new HashMap<>();

        map.put(pos.relative(isLower ? direction : direction.getOpposite()),
                blockState.setValue(HALF, isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER));

        return map;
    }

    @Override
    public StatePropertiesPredicate.Builder mainBlockPredicate() {
        return StatePropertiesPredicate.Builder.properties().hasProperty(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public boolean lookDirectionInfluencesPositions() {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        // Basic implementation for vertical double blocks but this can be overridden if needed
        return getPlacementHalf(context, Direction.Axis.Y)
                .map(doubleBlockHalf -> this.defaultBlockState().setValue(HALF, doubleBlockHalf))
                .orElse(null);
    }

    public static Optional<DoubleBlockHalf> getPlacementHalf(BlockItemUseContext context, Direction.Axis axis) {
        BlockPos pos = context.getClickedPos();
        boolean placedOnUpperSide = clickedOnPositiveHalf(context, axis);

        boolean canBeLower = canPlaceAt(context, pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE)));
        boolean canBeUpper = canPlaceAt(context, pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE)));

        // Placement preference
        if (!placedOnUpperSide && canBeUpper) return Optional.of(DoubleBlockHalf.UPPER);
        if (placedOnUpperSide && canBeLower) return Optional.of(DoubleBlockHalf.LOWER);

        // Placement fallback
        if (canBeUpper) return Optional.of(DoubleBlockHalf.UPPER);
        if (canBeLower) return Optional.of(DoubleBlockHalf.LOWER);

        return Optional.empty();
    }
}
