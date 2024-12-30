package net.portalmod.common.sorted.pellet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.portalmod.common.blocks.QuadBlock;
import net.portalmod.common.sorted.button.QuadBlockCorner;

public class PelletLauncherBlock extends QuadBlock {

    public PelletLauncherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(CORNER, QuadBlockCorner.UP_LEFT)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, CORNER);
    }
}
