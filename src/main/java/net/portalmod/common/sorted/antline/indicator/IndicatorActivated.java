package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Interface for block which can be activated by Antlines, using Indicators.
 */
public interface IndicatorActivated {
    /**
     * Returns a list of positions to check for antlines.
     */
    List<BlockPos> getIndicatorPositions(BlockState blockState, World world, BlockPos pos);

    /**
     * Checks for antlines in the positions given by {@link IndicatorActivated#getIndicatorPositions(BlockState, World, BlockPos)}.
     */
    default IndicatorInfo checkIndicators(BlockState blockState, World world, BlockPos pos) {
        return checkPositions(world, this.getIndicatorPositions(blockState, world, pos));
    }

    /**
     * Checks for antlines in a set of positions.
     */
    static IndicatorInfo checkPositions(World world, List<BlockPos> positions) {
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

