package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public class StepBlock extends Block implements IWaterLoggable {

    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty PILLAR = BooleanProperty.create("pillar");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StepBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, Half.BOTTOM)
                .setValue(PILLAR, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF, PILLAR, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return state.getValue(HALF) == Half.BOTTOM ? Block.box(0, 3, 0, 16, 8, 16) : Block.box(0, 11, 0, 16, 16, 16);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction clickedFace = context.getClickedFace();
        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        boolean pillar = context.getLevel().getBlockState(context.getClickedPos().below()).getBlock() instanceof StepPillarBlock;

        BlockState state = this.defaultBlockState().setValue(WATERLOGGED, waterlogged).setValue(PILLAR, pillar);

        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return state.setValue(HALF, clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Half.BOTTOM : Half.TOP);
        } else {
            boolean placedOnLowerSide = context.getClickLocation().y - context.getClickedPos().getY() < 0.5;
            return state.setValue(HALF, placedOnLowerSide ? Half.BOTTOM : Half.TOP);
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState state, IWorld world, BlockPos pos, BlockPos pos2) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        boolean hasPillar = world.getBlockState(pos.below()).getBlock() instanceof StepPillarBlock;
        return blockState.setValue(PILLAR, hasPillar);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }
}
