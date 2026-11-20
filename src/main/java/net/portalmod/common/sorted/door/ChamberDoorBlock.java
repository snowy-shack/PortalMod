package net.portalmod.common.sorted.door;

import com.mojang.math.Axis;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.*;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class ChamberDoorBlock extends MultiBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
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

    public ChamberDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SIDE, Side.LEFT));
    }

    public enum Side implements StringRepresentable {
        LEFT,
        RIGHT;

        public String toString() {
            return this.getSerializedName();
        }

        public String getSerializedName() {
            return this == LEFT ? "left" : "right";
        }

        public Side flip() {
            return this == LEFT ? RIGHT : LEFT;
        }
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
    public List<BlockPos> getConnectedPositions(BlockState mainState, BlockPos mainPos) {
        Direction horizontal = mainState.getValue(FACING).getCounterClockWise();
        return new ArrayList<>(Arrays.asList(
                mainPos.above(),
                mainPos.above().relative(horizontal),
                mainPos.relative(horizontal)
        ));
    }

    @Override
    public Map<BlockPos, BlockState> getOtherParts(BlockState blockState, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(SIDE) == Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();
        DoubleBlockHalf oppositeHalf = isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        Side oppositeSide = isLeft ? Side.RIGHT : Side.LEFT;

        HashMap<BlockPos, BlockState> map = new HashMap<>();

        // above/below placed block
        map.put(pos.relative(vertical), blockState.setValue(HALF, oppositeHalf));

        // next to placed block
        map.put(pos.relative(horizontal), blockState.setValue(SIDE, oppositeSide));

        // diagonal to placed block
        map.put(pos.relative(horizontal).relative(vertical), blockState.setValue(SIDE, oppositeSide).setValue(HALF, oppositeHalf));

        return map;
    }

    @Override
    public boolean isSamePart(BlockState one, BlockState two) {
        return one.getValue(FACING) == two.getValue(FACING)
                && one.getValue(HALF) == two.getValue(HALF)
                && one.getValue(SIDE) == two.getValue(SIDE);
    }

    @Override
    public void addMainBlockProperties(Map<Property<?>, Comparable<?>> map) {
        map.put(HALF, DoubleBlockHalf.LOWER);
        map.put(SIDE, Side.LEFT);
    }

    @Override
    public boolean lookDirectionInfluencesLocation() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF, SIDE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int facing = state.getValue(FACING).get2DDataValue() * 90;
        int side = state.getValue(SIDE) == Side.LEFT ? 0 : 180;
        VoxelShapeGroup shapeGroup = state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER : LOWER;

        Mat4 matrix = Mat4.identity();
        matrix.translate(new Vec3(.5));
        matrix.rotateDeg(Axis.YN, facing + side);
        matrix.translate(new Vec3(-.5));

        return shapeGroup.clone()
                .transform(matrix)
                .getVariant(state.getValue(OPEN) ? "open" : "closed");
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
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
            net.minecraft.world.phys.Vec3 clickOffset = context.getClickLocation().subtract(net.minecraft.world.phys.Vec3.atCenterOf(pos));
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

    public static boolean canPlace(BlockPos[] posArray, BlockPlaceContext context, Level world) {
        for (BlockPos pos : posArray) {
            if (!ModUtil.canPlaceAt(context, pos)) {
                return false;
            }
        }
        return true;
    }

    public void setOpen(boolean open, BlockState blockState, Level world, BlockPos pos) {
        this.setBlockStateValue(OPEN, open, blockState, world, pos);
        this.updateAllNeighbors(world, pos, blockState);
        playSound(open, blockState, world, pos);
    }

    public static void playSound(boolean open, BlockState blockState, Level world, BlockPos pos) {
        net.minecraft.world.phys.Vec3 middlePos = getExactMiddlePos(blockState, pos);
        world.playSound(null, middlePos.x, middlePos.y, middlePos.z, open ? SoundInit.CHAMBER_DOOR_OPEN.get() : SoundInit.CHAMBER_DOOR_CLOSE.get(), SoundSource.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    public static net.minecraft.world.phys.Vec3 getExactMiddlePos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return net.minecraft.world.phys.Vec3.atBottomCenterOf(pos).add(new Vec3(facing.getCounterClockWise().getNormal()).mul(0.5).add(0, 1, 0).to3d());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(this.getMainPosition(state, pos));
        if (state.getValue(OPEN) && blockEntity instanceof ChamberDoorTileEntity) {
            return ((ChamberDoorTileEntity) blockEntity).isAutomatic() ? 15 : 0;
        }
        return 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return isMainBlock(state);
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return isMainBlock(state) ? TileEntityTypeInit.CHAMBER_DOOR.get().create() : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);

        if (mirror == Mirror.NONE) {
            return state;
        }

        BlockState sideFlipped = state.setValue(SIDE, state.getValue(SIDE).flip());

        if (mirror.mirror(facing) == facing) {
            return sideFlipped;
        }

        return sideFlipped.setValue(FACING, facing.getOpposite());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("chamber_door", list);
    }
}