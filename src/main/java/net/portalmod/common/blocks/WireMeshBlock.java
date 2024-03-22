package net.portalmod.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WireMeshBlock extends BreakableBlock {
    public WireMeshBlock(Properties properties) {
        super(properties);
    }

    public float getShadeBrightness(BlockState state, IBlockReader level, BlockPos pos) {
       return 1.0F;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos) {
        return true;
    }
}