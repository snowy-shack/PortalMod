package net.portalmod.common.blocks;

import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.*;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

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
    public List<BlockPos> getConnectedPositions(BlockState mainState, BlockPos mainPos) {
        return new ArrayList<>(Collections.singletonList(mainPos.relative(this.getUpperDirection(mainState))));
    }

    @Override
    public Map<BlockPos, BlockState> getOtherParts(BlockState blockState, BlockPos pos) {
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        Direction direction = this.getUpperDirection(blockState);

        HashMap<BlockPos, BlockState> map = new HashMap<>();

        map.put(pos.relative(isLower ? direction : direction.getOpposite()),
                blockState.setValue(HALF, isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER));

        return map;
    }

    @Override
    public boolean isSamePart(BlockState one, BlockState two) {
        return one.getValue(HALF) == two.getValue(HALF)
                && this.getUpperDirection(one) == this.getUpperDirection(two);
    }

    @Override
    public void addMainBlockProperties(Map<Property<?>, Comparable<?>> map) {
        map.put(HALF, DoubleBlockHalf.LOWER);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Basic implementation for vertical double blocks but this can be overridden if needed
        return getPlacementHalf(context, Direction.UP)
                .map(doubleBlockHalf -> this.defaultBlockState().setValue(HALF, doubleBlockHalf))
                .orElse(null);
    }

    public static Optional<DoubleBlockHalf> getPlacementHalf(BlockPlaceContext context, Direction upDirection) {
        BlockPos pos = context.getClickedPos();
        boolean placedOnUpperSide = clickedOnPositiveHalf(context, upDirection);

        boolean canBeLower = ModUtil.canPlaceAt(context, pos.relative(upDirection));
        boolean canBeUpper = ModUtil.canPlaceAt(context, pos.relative(upDirection.getOpposite()));

        // Placement preference
        if (!placedOnUpperSide && canBeUpper) return Optional.of(DoubleBlockHalf.UPPER);
        if (placedOnUpperSide && canBeLower) return Optional.of(DoubleBlockHalf.LOWER);

        // Placement fallback
        if (canBeUpper) return Optional.of(DoubleBlockHalf.UPPER);
        if (canBeLower) return Optional.of(DoubleBlockHalf.LOWER);

        return Optional.empty();
    }
}
