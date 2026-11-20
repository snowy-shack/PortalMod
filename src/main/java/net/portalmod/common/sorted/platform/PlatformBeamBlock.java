package net.portalmod.common.sorted.platform;

import net.portalmod.common.blocks.CustomPushBehavior;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class PlatformBeamBlock extends Block implements SimpleWaterloggedBlock, CustomPushBehavior, BeamBearer {

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

    public static boolean isBeamItem(Item item) {
        return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof PlatformBeamBlock;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING).getAxis()) {
            case X: return COLLISION_SHAPE_X;
            case Y: return COLLISION_SHAPE_Y;
            default: return COLLISION_SHAPE_Z;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING).getAxis()) {
            case X: return SHAPE_X;
            case Y: return SHAPE_Y;
            default: return SHAPE_Z;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER)
                .setValue(FACING, this.getPlacementDirection(context));
    }

    public Direction getPlacementDirection(BlockPlaceContext context) {
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        Block block = clickedState.getBlock();

        if (block instanceof BeamBearer) {
            Direction beamDirection = ((BeamBearer) block).getBeamDirection(clickedState);
            if (beamDirection != null && beamDirection.getAxis() == context.getClickedFace().getAxis()) {
                return beamDirection;
            }
        }

        return context.getClickedFace();
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState state, LevelAccessor world, BlockPos pos, BlockPos pos2) {
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
        // For Create compatibility, we only want to stick to other things if they are sticky too, like slime blocks.
        // Setting this to true means we are able to stick to the other block in all directions, which is not what we want.
        // The sticky functionality on just one axis is handled by ourselves, and does not use this method.
        return this.canStickTo(other);
    }

    @Override
    public boolean isStickyToNeighbor(Level level, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction dir, Direction moveDir) {
        return dir.getAxis() == state.getValue(FACING).getAxis();
    }

    @Nullable
    @Override
    public Direction getBeamDirection(BlockState state) {
        return state.getValue(FACING);
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
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("platform_beam", list);
    }
}
