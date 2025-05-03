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
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.portalmod.common.sorted.button.QuadBlockCorner;
import net.portalmod.core.math.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
        QuadBlockCorner base = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);

        for(QuadBlockCorner corner : QuadBlockCorner.values()) {
            if(corner == base)
                continue;
            world.setBlock(getOtherBlock(pos, base, corner, facing), blockState.setValue(CORNER, corner), Constants.BlockFlags.DEFAULT);
        }
    }

    @Override
    public StatePropertiesPredicate.Builder mainBlockPredicate() {
        return StatePropertiesPredicate.Builder.properties().hasProperty(CORNER, QuadBlockCorner.UP_LEFT);
    }

    public boolean checkEachBlock(IWorldReader level, BlockPos pos, QuadBlockCorner corner, Direction facing, Predicate<BlockState> p) {
        for(BlockPos targetPos : this.getAllBlocks(pos, corner, facing))
            if(p.test(level.getBlockState(targetPos)))
                return false;
        return true;
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
        Vec3 playerView = new Vec3(context.getPlayer().getViewVector(1));
        Direction direction = context.getClickedFace();
        Direction.Axis axis = direction.getAxis();
        QuadBlockCorner corner;

        Tuple<Direction, Direction> directions = placementDirectionsFromFacing(axis);
        Direction a = directions.getA();
        Direction b = directions.getB();

        if(direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            if(direction.getAxis() == Direction.Axis.X)
                playerView.z *= -1;
            else
                playerView.x *= -1;
        }

        double x = a.getAxisDirection().getStep() * -playerView.to3d().get(a.getAxis());
        double y = b.getAxisDirection().getStep() * -playerView.to3d().get(b.getAxis());
        corner = QuadBlockCorner.fromCoords(x, y);
//        System.out.println(x);
//        System.out.println(y);

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);

        boolean xNeg = false;
        boolean yNeg = false;

        if(x < y)
            xNeg = true;
        else
            yNeg = true;

        corner = QuadBlockCorner.fromCoords(x * (xNeg ? -1 : 1), y * (yNeg ? -1 : 1));

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);

        corner = QuadBlockCorner.fromCoords(x * (xNeg ? 1 : -1), y * (yNeg ? 1 : -1));

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);

        corner = QuadBlockCorner.fromCoords(-x, -y);

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);

        return null;
    }

    private boolean isPlaceable(BlockItemUseContext context, QuadBlockCorner corner) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return checkEachBlock(level, pos, corner, context.getClickedFace(), s -> !s.canBeReplaced(context))
                && canSurvive(level, pos, corner, context.getClickedFace());
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos) {
        return canSurvive(level, pos, state.getValue(CORNER), state.getValue(FACING));
    }

    private boolean canSurvive(IWorldReader level, BlockPos pos, QuadBlockCorner corner, Direction facing) {
        for(BlockPos targetPos : this.getAllBlocks(pos, corner, facing))
            if(!canSupportCenter(level, targetPos.relative(facing.getOpposite()), facing))
                return false;
        return true;
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
