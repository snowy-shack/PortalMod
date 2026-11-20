package net.portalmod.common.sorted.antline.indicator;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for blocks that can be activated by Antlines, using Indicators.
 */
public interface IndicatorActivated {
    /**
     * Returns a list of positions to check for antlines.
     */
    List<BlockPos> getIndicatorPositions(BlockState blockState, Level world, BlockPos pos);

    /**
     * Checks for antlines in the positions given by {@link IndicatorActivated#getIndicatorPositions(BlockState, Level, BlockPos)}.
     */
    default IndicatorInfo checkIndicators(BlockState blockState, Level world, BlockPos pos) {
        return checkPositions(world, this.getIndicatorPositions(blockState, world, pos));
    }

    /**
     * Checks for antlines in a set of positions.
     */
    static IndicatorInfo checkPositions(Level world, List<BlockPos> positions) {
        int totalIndicators = 0;
        int activeIndicators = 0;
        for (BlockPos indicatorPos : positions) {
            BlockState currentState = world.getBlockState(indicatorPos);
            Block block = currentState.getBlock();
            if (block instanceof TestElementActivator) {
                totalIndicators++;
                if (((TestElementActivator) block).isActive(currentState)) {
                    activeIndicators++;
                }
            }
        }
        return new IndicatorInfo(totalIndicators, activeIndicators);
    }
}

