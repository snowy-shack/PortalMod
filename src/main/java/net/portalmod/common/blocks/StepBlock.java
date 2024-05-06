package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class StepBlock extends Block {

    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    public StepBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return state.getValue(HALF) == Half.BOTTOM ? Block.box(0, 3, 0, 16, 8, 16) : Block.box(0, 11, 0, 16, 16, 16);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return this.defaultBlockState().setValue(HALF, clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Half.BOTTOM : Half.TOP);
        } else {
            boolean placedOnLowerSide = context.getClickLocation().y - context.getClickedPos().getY() < 0.5;
            return this.defaultBlockState().setValue(HALF, placedOnLowerSide ? Half.BOTTOM : Half.TOP);
        }
    }
}
