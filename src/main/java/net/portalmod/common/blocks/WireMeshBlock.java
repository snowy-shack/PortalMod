package net.portalmod.common.blocks;

import net.fabricmc.api.Environment;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class WireMeshBlock extends HalfTransparentBlock {
    public WireMeshBlock(Properties properties) {
        super(properties);
    }

    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
       return 1.0F;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}