package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.portalmod.core.HorizontalAxis;

public class ChamberLightsBlock extends Block {
    public static final EnumProperty<HorizontalAxis> AXIS = EnumProperty.create("axis", HorizontalAxis.class);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    
    public ChamberLightsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, HorizontalAxis.X)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, HALF);
    }
}