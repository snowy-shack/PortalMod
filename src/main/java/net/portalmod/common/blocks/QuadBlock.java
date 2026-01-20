package net.portalmod.common.blocks;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.button.QuadBlockCorner;

import javax.annotation.Nullable;
import java.util.*;

public class QuadBlock extends MultiBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<QuadBlockCorner> CORNER = EnumProperty.create("corner", QuadBlockCorner.class);

    public QuadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        QuadBlockCorner corner = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);

        Direction horizontal = facing == Direction.UP ? Direction.WEST : facing == Direction.DOWN ? Direction.EAST : facing.getClockWise();
        Direction vertical = facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;

        if (!corner.isLeft()) {
            pos = pos.relative(horizontal);
        }
        if (!corner.isUp()) {
            pos = pos.relative(vertical);
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        Direction facing = blockState.getValue(FACING);

        Direction horizontal = facing == Direction.UP ? Direction.EAST : facing == Direction.DOWN ? Direction.WEST : facing.getCounterClockWise();
        Direction vertical = facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.DOWN;

        return new ArrayList<>(Arrays.asList(
                mainPos.relative(horizontal),
                mainPos.relative(horizontal).relative(vertical),
                mainPos.relative(vertical)
        ));
    }

    @Override
    public Map<BlockPos, BlockState> getConnectedBlockStates(World world, BlockState blockState, BlockPos pos) {
        QuadBlockCorner base = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);

        Map<BlockPos, BlockState> map = new HashMap<>();

        for (QuadBlockCorner corner : QuadBlockCorner.values()) {
            if (corner == base) continue;
            map.put(getOtherBlock(pos, base, corner, facing), blockState.setValue(CORNER, corner));
        }

        return map;
    }

    @Override
    public StatePropertiesPredicate.Builder mainBlockPredicate() {
        return StatePropertiesPredicate.Builder.properties().hasProperty(CORNER, QuadBlockCorner.UP_LEFT);
    }

    @Override
    public boolean lookDirectionInfluencesPositions() {
        return false;
    }

    public List<BlockPos> getAllBlocks(BlockPos pos, QuadBlockCorner base, Direction facing) {
        List<BlockPos> poses = new ArrayList<>();
        for(QuadBlockCorner corner : QuadBlockCorner.values())
            poses.add(getOtherBlock(pos, base, corner, facing));
        return poses;
    }

    public BlockPos getOtherBlock(BlockPos pos, QuadBlockCorner base, QuadBlockCorner corner, Direction facing) {
        Tuple<Direction, Direction> directions = placementDirectionsFromFacing(facing.getAxis());
        Direction a = directions.getA();
        Direction b = directions.getB();
        int x = corner.getX() - base.getX();
        int y = corner.getY() - base.getY();

        if(facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            x *= -1;

        BlockPos newPos = new BlockPos(pos);
        if(x != 0) newPos = newPos.relative(x < 0 ? a.getOpposite() : a);
        if(y != 0) newPos = newPos.relative(y < 0 ? b.getOpposite() : b);

        return newPos;
    }

    public Tuple<Direction, Direction> placementDirectionsFromFacing(Direction.Axis axis) {
        if(axis == Direction.Axis.X)
            return new Tuple<>(Direction.NORTH, Direction.UP);
        if(axis == Direction.Axis.Z)
            return new Tuple<>(Direction.EAST, Direction.UP);
        return new Tuple<>(Direction.EAST, Direction.NORTH);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getClickedFace();
        Direction.Axis axis = direction.getAxis();
        boolean isPositive = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;

        // Determine directions of the corners for this placement face
        Direction upDirection = axis == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
        Direction leftDirection = axis == Direction.Axis.Y ? (isPositive ? Direction.WEST : Direction.EAST) : direction.getClockWise();

        boolean prefersUp = clickedOnPositiveHalf(context, upDirection.getOpposite());
        boolean prefersLeft = clickedOnPositiveHalf(context, leftDirection.getOpposite());

        boolean[] flipUp   = {false, false, true, true};
        boolean[] flipLeft = {false, true, false, true};

        for (int i = 0; i < 4; i++) {
            QuadBlockCorner corner = QuadBlockCorner.getCorner(prefersUp ^ flipUp[i], prefersLeft ^ flipLeft[i]);

            if (this.isCornerPlaceable(context, corner)) {
                return this.defaultBlockState()
                        .setValue(FACING, direction)
                        .setValue(CORNER, corner);
            }
        }

        return null;
    }

    public boolean isCornerPlaceable(BlockItemUseContext context, QuadBlockCorner corner) {
        return this.getAllBlocks(context.getClickedPos(), corner, context.getClickedFace()).stream()
                .allMatch(pos -> canPlaceAt(context, pos));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
