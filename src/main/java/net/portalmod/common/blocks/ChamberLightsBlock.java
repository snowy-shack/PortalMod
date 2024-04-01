package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChamberLightsBlock extends DoubleBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    
    public ChamberLightsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos clickedPos = context.getClickedPos();
        World world = context.getLevel();
        if (world.getBlockState(clickedPos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
        }
        else if (world.getBlockState(clickedPos.below()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()).setValue(HALF, DoubleBlockHalf.UPPER);
        }
        return null;
    }
}