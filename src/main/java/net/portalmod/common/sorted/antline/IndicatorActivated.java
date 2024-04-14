package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface IndicatorActivated {
    List<BlockPos> getIndicatorPositions(BlockState blockState, BlockPos pos);

    default IndicatorInfo checkIndicators(BlockState blockState, World world, BlockPos pos) {
        int totalIndicators = 0;
        int activeIndicators = 0;
        for (BlockPos indicatorPos : this.getIndicatorPositions(blockState, pos)) {
            BlockState currentState = world.getBlockState(indicatorPos);
            if (currentState.getBlock() instanceof AntlineIndicatorBlock) {
                totalIndicators++;
                if (AntlineIndicatorBlock.isOn(currentState)) {
                    activeIndicators++;
                }
            }
        }
        return new IndicatorInfo(totalIndicators, activeIndicators);
    }
}

