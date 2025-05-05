package net.portalmod.common.sorted.door;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberDoorBlock extends MultiBlock {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);

    private static final VoxelShapeGroup UPPER = new VoxelShapeGroup.Builder()
            .add(0, 0, 3, 2, 16, 13)
            .add(0, 14, 3, 16, 16, 13)
            .addPart("closed", 2, 0, 5, 16, 14, 11)
            .addPart("open", 2, 0, 5, 5, 14, 11)
            .build();

    private static final VoxelShapeGroup LOWER = new VoxelShapeGroup.Builder()
            .add(0, 0, 3, 2, 16, 13)
            .addPart("closed", 0, 0, 5, 16, 16, 11)
            .addPart("open", 0, 0, 5, 5, 16, 11)
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
    public StatePropertiesPredicate.Builder mainBlockPredicate() {
        return StatePropertiesPredicate.Builder.properties().hasProperty(HALF, DoubleBlockHalf.LOWER).hasProperty(SIDE, Side.LEFT);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF, SIDE);
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
        Direction clickedFace = context.getClickedFace();

        BlockState rotated = this.defaultBlockState().setValue(FACING, facing.getOpposite());

        List<Tuple<DoubleBlockHalf, Side>> possibleStates = new ArrayList<>();

        if (canPlace(new BlockPos[]{
                pos.above(),
                pos.relative(facing.getClockWise()),
                pos.relative(facing.getClockWise()).above()
        }, context, world)) {
            possibleStates.add(new Tuple<>(DoubleBlockHalf.LOWER, Side.LEFT));
        }

        if (canPlace(new BlockPos[]{
                pos.below(),
                pos.relative(facing.getClockWise()),
                pos.relative(facing.getClockWise()).below()
        }, context, world)) {
            possibleStates.add(new Tuple<>(DoubleBlockHalf.UPPER, Side.LEFT));
        }

        if (canPlace(new BlockPos[]{
                pos.above(),
                pos.relative(facing.getCounterClockWise()),
                pos.relative(facing.getCounterClockWise()).above()
        }, context, world)) {
            possibleStates.add(new Tuple<>(DoubleBlockHalf.LOWER, Side.RIGHT));
        }

        if (canPlace(new BlockPos[]{
                pos.below(),
                pos.relative(facing.getCounterClockWise()),
                pos.relative(facing.getCounterClockWise()).below()
        }, context, world)) {
            possibleStates.add(new Tuple<>(DoubleBlockHalf.UPPER, Side.RIGHT));
        }

        if (possibleStates.isEmpty()) {
            return null;
        }

        if (clickedFace == Direction.UP) {
            Vector3d clickOffset = context.getClickLocation().subtract(Vector3d.atCenterOf(pos));
            double perpendicular = facing.getAxis() == Direction.Axis.X ?
                    facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                            clickOffset.z : -clickOffset.z :
                    facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                            -clickOffset.x : clickOffset.x;
            Tuple<DoubleBlockHalf, Side> preferredState = new Tuple<>(DoubleBlockHalf.LOWER, perpendicular > 0 ? Side.LEFT : Side.RIGHT);
            if (possibleStates.stream().anyMatch(tuple -> tuple.getA() == preferredState.getA() && tuple.getB() == preferredState.getB())) {
                return rotated.setValue(HALF, preferredState.getA()).setValue(SIDE, preferredState.getB());
            }
        }

        return rotated.setValue(HALF, possibleStates.get(0).getA()).setValue(SIDE, possibleStates.get(0).getB());
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
        playSound(open, blockState, world, pos);
    }

    public static void playSound(boolean open, BlockState blockState, World world, BlockPos pos) {
        Vector3d middlePos = getExactMiddlePos(blockState, pos);
        world.playSound(null, middlePos.x, middlePos.y, middlePos.z, open ? SoundInit.CHAMBER_DOOR_OPEN.get() : SoundInit.CHAMBER_DOOR_CLOSE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    public static Vector3d getExactMiddlePos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return Vector3d.atBottomCenterOf(pos).add(new Vec3(facing.getCounterClockWise().getNormal()).mul(0.5).add(0, 1, 0).to3d());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return isMainBlock(state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return isMainBlock(state) ? TileEntityTypeInit.CHAMBER_DOOR.get().create() : null;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("chamber_door", list);
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