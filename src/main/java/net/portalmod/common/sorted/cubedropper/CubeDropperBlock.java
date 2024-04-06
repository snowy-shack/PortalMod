package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.common.sorted.superbutton.QuadBlockCorner;
import net.portalmod.core.init.TileEntityTypeInit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CubeDropperBlock extends MultiBlock {

    public static final EnumProperty<QuadBlockCorner> CORNER = EnumProperty.create("corner", QuadBlockCorner.class);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public CubeDropperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CORNER, QuadBlockCorner.UP_LEFT).setValue(HALF, DoubleBlockHalf.UPPER).setValue(OPEN, false));
    }

    /*
              North

     West    main  UR    East
              DL   DR

               South
     */

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        if (!blockState.getValue(CORNER).isLeft()) {
            pos = pos.relative(Direction.WEST);
        }
        if (!blockState.getValue(CORNER).isUp()) {
            pos = pos.relative(Direction.NORTH);
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            pos = pos.relative(Direction.UP);
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        return new ArrayList<>(Arrays.asList(
                mainPos.relative(Direction.EAST),
                mainPos.relative(Direction.SOUTH),
                mainPos.relative(Direction.SOUTH).relative(Direction.EAST),
                mainPos.below(),
                mainPos.below().relative(Direction.EAST),
                mainPos.below().relative(Direction.SOUTH),
                mainPos.below().relative(Direction.SOUTH).relative(Direction.EAST)
        ));
    }

    @Override
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
        QuadBlockCorner corner = blockState.getValue(CORNER);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = corner.isLeft();
        boolean isUp = corner.isUp();

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction leftRight = isLeft ? Direction.EAST : Direction.WEST;
        Direction upDown = isUp ? Direction.SOUTH : Direction.NORTH;

        DoubleBlockHalf oppositeHalf = isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        QuadBlockCorner oppositeLeftRight = corner.mirrorLeftRight();
        QuadBlockCorner oppositeUpDown = corner.mirrorUpDown();
        QuadBlockCorner diagonal = corner.mirrorUpDown().mirrorLeftRight();

        world.setBlockAndUpdate(pos.relative(leftRight), blockState.setValue(CORNER, oppositeLeftRight));

        world.setBlockAndUpdate(pos.relative(upDown), blockState.setValue(CORNER, oppositeUpDown));

        world.setBlockAndUpdate(pos.relative(upDown).relative(leftRight), blockState.setValue(CORNER, diagonal));

        world.setBlockAndUpdate(pos.relative(vertical), blockState.setValue(HALF, oppositeHalf));

        world.setBlockAndUpdate(pos.relative(vertical).relative(leftRight), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, oppositeLeftRight));

        world.setBlockAndUpdate(pos.relative(vertical).relative(upDown), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, oppositeUpDown));

        world.setBlockAndUpdate(pos.relative(vertical).relative(upDown).relative(leftRight), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, diagonal));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (context.getClickedFace() != Direction.DOWN) {
            return null;
        }

        if (canPlace(new BlockPos[]{
                pos.relative(Direction.EAST),
                pos.relative(Direction.SOUTH),
                pos.relative(Direction.SOUTH).relative(Direction.EAST),
                pos.below(),
                pos.below().relative(Direction.EAST),
                pos.below().relative(Direction.SOUTH),
                pos.below().relative(Direction.SOUTH).relative(Direction.EAST)
        }, context, world)) {
            return this.defaultBlockState();
        }
        else if (canPlace(new BlockPos[]{
                pos.relative(Direction.WEST),
                pos.relative(Direction.SOUTH),
                pos.relative(Direction.SOUTH).relative(Direction.WEST),
                pos.below(),
                pos.below().relative(Direction.WEST),
                pos.below().relative(Direction.SOUTH),
                pos.below().relative(Direction.SOUTH).relative(Direction.WEST)
        }, context, world)) {
            return this.defaultBlockState().setValue(CORNER, QuadBlockCorner.UP_RIGHT);
        }
        else if (canPlace(new BlockPos[]{
                pos.relative(Direction.WEST),
                pos.relative(Direction.NORTH),
                pos.relative(Direction.NORTH).relative(Direction.WEST),
                pos.below(),
                pos.below().relative(Direction.WEST),
                pos.below().relative(Direction.NORTH),
                pos.below().relative(Direction.NORTH).relative(Direction.WEST)
        }, context, world)) {
            return this.defaultBlockState().setValue(CORNER, QuadBlockCorner.DOWN_RIGHT);
        }
        else if (canPlace(new BlockPos[]{
                pos.relative(Direction.EAST),
                pos.relative(Direction.NORTH),
                pos.relative(Direction.NORTH).relative(Direction.EAST),
                pos.below(),
                pos.below().relative(Direction.EAST),
                pos.below().relative(Direction.NORTH),
                pos.below().relative(Direction.NORTH).relative(Direction.EAST)
        }, context, world)) {
            return this.defaultBlockState().setValue(CORNER, QuadBlockCorner.DOWN_LEFT);
        }
        return null;
    }

    public static boolean canPlace(BlockPos[] posArray, BlockItemUseContext context, World world) {
        for (BlockPos pos : posArray) {
            if (!world.getBlockState(pos).canBeReplaced(context)) {
                return false;
            }
        }
        return true;
    }

    public void setOpen(boolean open, BlockState blockState, World world, BlockPos pos) {
        this.setBlockStateValue(OPEN, open, blockState, world, pos);
    }

    public void setPowered(boolean powered, BlockState blockState, World world, BlockPos pos) {
        this.setBlockStateValue(POWERED, powered, blockState, world, pos);
    }

    @Override
    public void neighborChanged(BlockState blockState, World world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_, boolean p_220069_6_) {
        if (world.isClientSide) {
            return;
        }

        boolean wasPowered = blockState.getValue(POWERED);
        boolean isPowered = false;
        for (BlockPos checkingPos : getAllPositions(blockState, pos)) {
            if (world.hasNeighborSignal(checkingPos)) {
                isPowered = true;
            }
        }

        if (wasPowered != isPowered) {
            setPowered(isPowered, blockState, world, pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(CORNER, HALF, POWERED, OPEN);
    }

    public static boolean isMainBlock(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER && state.getValue(CORNER) == QuadBlockCorner.UP_LEFT;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return isMainBlock(state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return isMainBlock(state) ? TileEntityTypeInit.CUBE_DROPPER.get().create() : null;
    }
}
