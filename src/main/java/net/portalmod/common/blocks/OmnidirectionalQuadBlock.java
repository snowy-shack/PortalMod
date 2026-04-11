package net.portalmod.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.portalmod.common.sorted.button.QuadBlockCorner;

import javax.annotation.Nullable;
import java.util.*;

public class OmnidirectionalQuadBlock extends QuadBlock {
    public static final DirectionProperty DIRECTION = DirectionProperty.create("direction", Direction.Plane.HORIZONTAL);

    public OmnidirectionalQuadBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getClickedFace();
        Direction horizontalDirection = context.getHorizontalDirection();
        Direction.Axis axis = direction.getAxis();

        // Determine directions of the corners for this placement face
        Direction upDirection = axis == Direction.Axis.Y ? horizontalDirection : Direction.UP;
        Direction leftDirection = axis == Direction.Axis.Y ? (
                direction == Direction.UP ? horizontalDirection.getCounterClockWise() : horizontalDirection.getClockWise()
        ) : direction.getClockWise();

        boolean prefersUp = clickedOnPositiveHalf(context, upDirection.getOpposite());
        boolean prefersLeft = clickedOnPositiveHalf(context, leftDirection.getOpposite());

        boolean[] flipUp   = {false, false, true, true};
        boolean[] flipLeft = {false, true, false, true};

        for (int i = 0; i < 4; i++) {
            QuadBlockCorner corner = QuadBlockCorner.getCorner(prefersUp ^ flipUp[i], prefersLeft ^ flipLeft[i]);

            if (this.isCornerPlaceable(context, corner)) {
                BlockState blockState = this.defaultBlockState()
                        .setValue(FACING, direction)
                        .setValue(CORNER, corner);

                if(direction.getAxis() == Direction.Axis.Y) {
                    blockState = blockState.setValue(DIRECTION, horizontalDirection);
                }

                return blockState;
            }
        }

        return null;
    }

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        QuadBlockCorner corner = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);
        Direction direction = blockState.getValue(DIRECTION);

        Direction horizontal = facing == Direction.UP ? direction.getCounterClockWise() : facing == Direction.DOWN ? direction.getClockWise() : facing.getClockWise();
        Direction vertical = facing.getAxis() == Direction.Axis.Y ? direction : Direction.UP;

        if (!corner.isLeft()) {
            pos = pos.relative(horizontal);
        }
        if (!corner.isUp()) {
            pos = pos.relative(vertical);
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState mainState, BlockPos mainPos) {
        Direction facing = mainState.getValue(FACING);
        Direction direction = mainState.getValue(DIRECTION);

        Direction horizontal = facing == Direction.UP ? direction.getClockWise() : facing == Direction.DOWN ? direction.getCounterClockWise() : facing.getCounterClockWise();
        Direction vertical = facing.getAxis() == Direction.Axis.Y ? direction.getOpposite() : Direction.DOWN;

        return new ArrayList<>(Arrays.asList(
                mainPos.relative(horizontal),
                mainPos.relative(horizontal).relative(vertical),
                mainPos.relative(vertical)
        ));
    }

    public BlockPos getOtherBlock(BlockPos pos, QuadBlockCorner base, QuadBlockCorner corner, Direction facing, Direction direction) {
        Tuple<Direction, Direction> directions = placementDirectionsFromFacingAndDirection(facing, direction);
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

    public Tuple<Direction, Direction> placementDirectionsFromFacingAndDirection(Direction facing, Direction direction) {
        if(facing.getAxis() == Direction.Axis.X)
            return new Tuple<>(Direction.NORTH, Direction.UP);
        if(facing.getAxis() == Direction.Axis.Z)
            return new Tuple<>(Direction.EAST, Direction.UP);
        return new Tuple<>(direction.getClockWise(), direction);
    }

    public List<BlockPos> getAllBlocks(BlockPos pos, QuadBlockCorner base, Direction facing, Direction direction) {
        List<BlockPos> poses = new ArrayList<>();
        for(QuadBlockCorner corner : QuadBlockCorner.values())
            poses.add(getOtherBlock(pos, base, corner, facing, direction));
        return poses;
    }

    @Override
    public Map<BlockPos, BlockState> getOtherParts(BlockState blockState, BlockPos pos) {
        QuadBlockCorner base = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);

        Map<BlockPos, BlockState> map = new HashMap<>();

        for (QuadBlockCorner corner : QuadBlockCorner.values()) {
            if (corner == base) continue;
            map.put(getOtherBlock(pos, base, corner, facing, blockState.getValue(DIRECTION)), blockState.setValue(CORNER, corner));
        }

        return map;
    }
}