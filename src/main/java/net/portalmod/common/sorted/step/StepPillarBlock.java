package net.portalmod.common.sorted.step;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.common.blocks.CustomPushBehavior;

import javax.annotation.Nullable;

public class StepPillarBlock extends Block implements IWaterLoggable, CustomPushBehavior {

    public static final VoxelShape SHAPE_X = Block.box(0, 5, 5, 16, 11, 11);
    public static final VoxelShape SHAPE_Y = Block.box(5, 0, 5, 11, 16, 11);
    public static final VoxelShape SHAPE_Z = Block.box(5, 5, 0, 11, 11, 16);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StepPillarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING).getAxis()) {
            case X: return SHAPE_X;
            case Y: return SHAPE_Y;
            default: return SHAPE_Z;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState()
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER)
                .setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState state, IWorld world, BlockPos pos, BlockPos pos2) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return blockState;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean isStickyToNeighbor(World level, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction dir, Direction moveDir) {
        return dir.getAxis() == state.getValue(FACING).getAxis();
    }
}
