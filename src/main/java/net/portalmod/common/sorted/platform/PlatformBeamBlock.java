package net.portalmod.common.sorted.platform;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.common.blocks.CustomPushBehavior;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class PlatformBeamBlock extends Block implements IWaterLoggable, CustomPushBehavior {

    public static final VoxelShape SHAPE_X = Block.box(0, 5, 5, 16, 11, 11);
    public static final VoxelShape SHAPE_Y = Block.box(5, 0, 5, 11, 16, 11);
    public static final VoxelShape SHAPE_Z = Block.box(5, 5, 0, 11, 11, 16);
    
    public static final VoxelShape COLLISION_SHAPE_X = Block.box(0, 4.5, 4.5, 16, 11.5, 11.5);
    public static final VoxelShape COLLISION_SHAPE_Y = Block.box(4.5, 0, 4.5, 11.5, 16, 11.5);
    public static final VoxelShape COLLISION_SHAPE_Z = Block.box(4.5, 4.5, 0, 11.5, 11.5, 16);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PlatformBeamBlock(Properties properties) {
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
    public VoxelShape getCollisionShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING).getAxis()) {
            case X: return COLLISION_SHAPE_X;
            case Y: return COLLISION_SHAPE_Y;
            default: return COLLISION_SHAPE_Z;
        }
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
                .setValue(FACING, this.getPlacementDirection(context));
    }

    public Direction getPlacementDirection(BlockItemUseContext context) {
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));

        // Ensure you don't randomly flip the beam if placing on another
        if (clickedState.getBlock() instanceof PlatformBeamBlock) {
            Direction facing = clickedState.getValue(PlatformBeamBlock.FACING);
            if (facing.getAxis() == context.getClickedFace().getAxis()) {
                return facing;
            }
        }

        // When hitting the underside of a platform, flip the beam the right side up
        if (clickedState.getBlock() instanceof PlatformBlock) {
            Direction facing = clickedState.getValue(PlatformBlock.FACING);
            if (facing.getOpposite() == context.getClickedFace()) {
                return facing;
            }
        }

        return context.getClickedFace();
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
    public boolean canStickTo(BlockState state, BlockState other) {
        return other.isStickyBlock();
    }

    @Override
    public boolean isStickyToNeighbor(World level, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction dir, Direction moveDir) {
        return dir.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("platform_beam", list);
    }
}
