package net.portalmod.common.sorted.fizzler;

import com.mojang.math.Axis;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class FizzlerFieldBlock extends DoubleBlock implements Fizzler {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");

    public FizzlerFieldBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(ROTATED, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, ROTATED, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getFieldShape(BlockState state) {
        VoxelShapeGroup group = new VoxelShapeGroup.Builder()
                .add(7, 0, 0, 9, 16, 16)
                .build();

        Direction.Axis axis = state.getValue(AXIS);
        boolean rotated = state.getValue(ROTATED);

        Mat4 matrix = Mat4.identity();
        matrix.translate(new Vec3(.5));

        if (axis == Direction.Axis.X) {
            matrix.rotateDeg(Axis.YP, 90);
        }

        if (axis == Direction.Axis.Y) {
            matrix.rotateDeg(Axis.XP, 90);
        }

        if (rotated) {
            matrix.rotateDeg(Axis.ZP, 90);
        }

        matrix.translate(new Vec3(-.5));

        return group.transform(matrix).getShape();
    }

    @Override
    public Direction getUpperDirection(BlockState state) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean rotated = state.getValue(ROTATED);

        if (axis == Direction.Axis.Y) {
            return rotated ? Direction.EAST : Direction.SOUTH;
        }

        if (axis == Direction.Axis.X) {
            return rotated ? Direction.SOUTH : Direction.UP;
        }

        return rotated ? Direction.EAST : Direction.UP;
    }

    @Override
    public boolean isInsideField(AABB box, BlockPos pos, BlockState state) {
        return this.getFieldShape(state).bounds().move(pos).intersects(box);
    }

    @Override
    public boolean isActive(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        Direction facing = Direction.fromAxisAndDirection(state.getValue(AXIS), Direction.AxisDirection.POSITIVE);
        BlockState leftBlock = world.getBlockState(pos.relative(facing));
        BlockState rightBlock = world.getBlockState(pos.relative(facing.getOpposite()));

        if (!this.isValidConnection(state, leftBlock, facing) || !(this.isValidConnection(state, rightBlock, facing.getOpposite()))) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    public boolean isValidConnection(BlockState state, BlockState neighbor, Direction direction) {
        if (neighbor.getBlock() instanceof FizzlerFieldBlock) {
            return neighbor.getValue(AXIS) == state.getValue(AXIS)
                    && neighbor.getValue(ROTATED) == state.getValue(ROTATED)
                    && neighbor.getValue(HALF) == state.getValue(HALF);
        }

        if (neighbor.getBlock() instanceof FizzlerEmitterBlock) {
            return neighbor.getValue(FizzlerEmitterBlock.FACING) == direction.getOpposite()
                    && neighbor.getValue(FizzlerEmitterBlock.ROTATED) == state.getValue(ROTATED)
                    && neighbor.getValue(FizzlerEmitterBlock.HALF) == state.getValue(HALF)
                    && neighbor.getValue(FizzlerEmitterBlock.ACTIVE);
        }

        return false;
    }
}