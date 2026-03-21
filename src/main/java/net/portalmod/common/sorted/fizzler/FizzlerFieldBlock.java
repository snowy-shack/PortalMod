package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

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
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
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
            matrix.rotateDeg(Vector3f.YP, 90);
        }

        if (axis == Direction.Axis.Y) {
            matrix.rotateDeg(Vector3f.XP, 90);
        }

        if (rotated) {
            matrix.rotateDeg(Vector3f.ZP, 90);
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
    public boolean isInsideField(AxisAlignedBB box, BlockPos pos, BlockState state) {
        return this.getFieldShape(state).bounds().move(pos).intersects(box);
    }

    @Override
    public boolean isActive(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
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

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        VoxelShape voxelshape = getFieldShape(state);
        VoxelShape movedBlockShape = voxelshape.move(pos.getX(), pos.getY(), pos.getZ());
        VoxelShape entityShape = VoxelShapes.create(entity.getBoundingBox());

        if (entity instanceof TestElementEntity) {
            if (VoxelShapes.joinIsNotEmpty(movedBlockShape, entityShape, IBooleanFunction.AND)) {
                ((TestElementEntity) entity).startFizzling();
            }
        }

        if(level.isClientSide)
            return;

        if(VoxelShapes.joinIsNotEmpty(movedBlockShape, entityShape, IBooleanFunction.AND)) {
            if (entity instanceof PlayerEntity) {
                PortalGun.fizzleGun(level, (PlayerEntity) entity);
            }

            if (entity instanceof ItemEntity && ((ItemEntity) entity).getItem().getItem() instanceof PortalGun) {
                PortalGun.fizzleGunItem(((ItemEntity) entity).getItem());
            }
        }
    }
}