package net.portalmod.common.sorted.chamberdoor;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberDoorBlock extends MultiBlock {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShapeGroup UPPER = new VoxelShapeGroup.Builder()
            .add(0, 0, 0, 2, 16, 16)
            .add(0, 14, 0, 16, 16, 16)
            .addPart("closed", 0, 0, 2, 16, 16, 14)
            .addPart("open", 0, 0, 2, 5, 16, 14)
            .build();

    private static final VoxelShapeGroup LOWER = new VoxelShapeGroup.Builder()
            .add(0, 0, 0, 2, 16, 16)
            .addPart("closed", 0, 0, 2, 16, 16, 14)
            .addPart("open", 0, 0, 2, 5, 16, 14)
            .build();

    public ChamberDoorBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SIDE, Side.LEFT));
    }

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        if (blockState.getValue(SIDE) == Side.RIGHT) {
            pos = pos.relative(blockState.getValue(FACING).getClockWise());
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.relative(Direction.DOWN);
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        Direction horizontal = blockState.getValue(FACING).getCounterClockWise();
        return new ArrayList<>(Arrays.asList(
                mainPos.above(),
                mainPos.above().relative(horizontal),
                mainPos.relative(horizontal)
        ));
    }

    @Override
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(SIDE) == Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();
        DoubleBlockHalf oppositeHalf = isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        Side oppositeSide = isLeft ? Side.RIGHT : Side.LEFT;

        // above/below placed block
        world.setBlockAndUpdate(pos.relative(vertical), blockState.setValue(HALF, oppositeHalf));

        // next to placed block
        world.setBlockAndUpdate(pos.relative(horizontal), blockState.setValue(SIDE, oppositeSide));

        // diagonal to placed block
        world.setBlockAndUpdate(pos.relative(horizontal).relative(vertical), blockState.setValue(SIDE, oppositeSide).setValue(HALF, oppositeHalf));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF, SIDE, POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        int facing = state.getValue(FACING).get2DDataValue() * 90;
        int side = state.getValue(SIDE) == Side.LEFT ? 0 : 180;
        VoxelShapeGroup shapeGroup = state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER : LOWER;

        Mat4 matrix = Mat4.identity();
        matrix.translate(new Vec3(.5));
        matrix.rotateDeg(Vector3f.YN, facing + side);
        matrix.translate(new Vec3(-.5));

        return shapeGroup.clone()
                .transform(matrix)
                .getVariant(state.getValue(OPEN) ? "open" : "closed");
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();

        BlockState rotated = this.defaultBlockState().setValue(FACING, facing.getOpposite());

        if (world.hasNeighborSignal(pos)) {
            rotated = rotated.setValue(OPEN, true).setValue(POWERED, true);
        }

        if (canPlace(new BlockPos[]{
                pos.above(),
                pos.relative(facing.getClockWise()),
                pos.relative(facing.getClockWise()).above()
        }, context, world)) {
            return rotated;
        }
        else if (canPlace(new BlockPos[]{
                pos.below(),
                pos.relative(facing.getClockWise()),
                pos.relative(facing.getClockWise()).below()
        }, context, world)) {
            return rotated.setValue(HALF, DoubleBlockHalf.UPPER);
        }
        else if (canPlace(new BlockPos[]{
                pos.above(),
                pos.relative(facing.getCounterClockWise()),
                pos.relative(facing.getCounterClockWise()).above()
        }, context, world)) {
            return rotated.setValue(SIDE, Side.RIGHT);
        }
        else if (canPlace(new BlockPos[]{
                pos.below(),
                pos.relative(facing.getCounterClockWise()),
                pos.relative(facing.getCounterClockWise()).below()
        }, context, world)) {
            return rotated.setValue(HALF, DoubleBlockHalf.UPPER).setValue(SIDE, Side.RIGHT);
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
        playSound(open, world, pos);
    }

    public void setPowered(boolean powered, BlockState blockState, World world, BlockPos pos) {
        this.setBlockStateValue(POWERED, powered, blockState, world, pos);
    }

    public static void playSound(boolean open, World world, BlockPos pos) {
        world.playSound(null, pos, open ? SoundInit.CHAMBER_DOOR_OPEN.get() : SoundInit.CHAMBER_DOOR_CLOSE.get(), SoundCategory.BLOCKS, 1, 1);
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
    public boolean hasTileEntity(BlockState state) {
        return isMainBlock(state);
    }

    public static boolean isMainBlock(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(SIDE) == Side.LEFT;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return isMainBlock(state) ? TileEntityTypeInit.CHAMBER_DOOR.get().create() : null;
    }

    public enum Side implements IStringSerializable {
        LEFT,
        RIGHT;

        public String toString() {
            return this.getSerializedName();
        }

        public String getSerializedName() {
            return this == LEFT ? "left" : "right";
        }
    }
}