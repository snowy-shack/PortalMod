package net.portalmod.common.sorted.fizzler;

import com.mojang.math.Axis;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class FizzlerEmitterBlock extends DoubleBlock implements Fizzler {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public FizzlerEmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ROTATED, false)
                .setValue(ACTIVE, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public VoxelShapeGroup getShapeGroup(BlockState state) {
        VoxelShapeGroup group = new VoxelShapeGroup.Builder()
            .add(0, 0, 3, 1, 16, 13)
            .addPart("active", Shapes.or(
                    Block.box(1, 0, 5, 3, 16, 11),
                    Block.box(1, 0, 6.5, 4.5, 15, 9.5)))
            .addPart("field", 0,0,7,16,16,9)
            .build();

        Direction facing = state.getValue(FACING);
        boolean rotated = state.getValue(ROTATED);
        boolean lower = state.getValue(HALF) == DoubleBlockHalf.LOWER;

        Mat4 matrix = Mat4.identity();
        matrix.translate(new Vec3(.5));

        if (facing.getAxis() == Direction.Axis.Y) {
            matrix.rotateDeg(Axis.ZP, facing == Direction.UP ? 90 : -90);

            if (!rotated) {
                matrix.rotateDeg(Axis.XP, facing == Direction.UP ? -90 : 90);
            }

            if (!lower ^ facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                matrix.scale(1, -1, 1);
            }
        } else {
            int angle = facing.get2DDataValue() * -90 - 90;
            matrix.rotateDeg(Axis.YP, angle);

            if (rotated) {
                matrix.rotateDeg(Axis.XP, -90);

                if (facing.getAxis() == Direction.Axis.X ^ facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                    matrix.scale(1, -1, 1);
                }
            }

            if (lower) {
                matrix.scale(1, -1, 1);
            }
        }

        matrix.translate(new Vec3(-.5));

        return group.transform(matrix);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShapeGroup(state).getVariant(state.getValue(ACTIVE) ? "active" : "");
    }

    @Override
    public VoxelShape getFieldShape(BlockState state) {
        return this.getShapeGroup(state).getPart("field");
    }

    @Override
    public Direction getUpperDirection(BlockState state) {
        Direction.Axis axis = state.getValue(FACING).getAxis();
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
        return state.getValue(FizzlerEmitterBlock.ACTIVE) && this.getFieldShape(state).bounds().move(pos).intersects(box);
    }

    @Override
    public boolean isActive(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        if (state.getValue(ACTIVE)) {
            Direction direction = state.getValue(FACING);
            BlockState neighbor = world.getBlockState(pos.relative(direction));
            if (!this.isValidConnection(state, neighbor)) {
                this.setBlockStateValue(ACTIVE, false, state, world, pos);
                this.updateAllNeighbors(world, pos, state);
            }
        }
    }

    public boolean isValidConnection(BlockState state, BlockState neighbor) {
        if (neighbor.getBlock() instanceof FizzlerFieldBlock) {
            return neighbor.getValue(FizzlerFieldBlock.AXIS) == state.getValue(FACING).getAxis()
                    && neighbor.getValue(FizzlerFieldBlock.ROTATED) == state.getValue(ROTATED)
                    && neighbor.getValue(FizzlerFieldBlock.HALF) == state.getValue(HALF);
        }

        if (neighbor.getBlock() instanceof FizzlerEmitterBlock) {
            return neighbor.getValue(FACING) == state.getValue(FACING).getOpposite()
                    && neighbor.getValue(HALF) == state.getValue(HALF)
                    && neighbor.getValue(ROTATED) == state.getValue(ROTATED)
                    && neighbor.getValue(ACTIVE);
        }

        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState state = this.defaultBlockState().setValue(FACING, clickedFace);

        // Floor / ceiling
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            boolean rotated = context.getHorizontalDirection().getAxis() == Direction.Axis.X;
            Optional<DoubleBlockHalf> half = getPlacementHalf(context, Direction.fromAxisAndDirection(
                    context.getHorizontalDirection().getAxis(), Direction.AxisDirection.POSITIVE));

            if (!half.isPresent()) return null;

            return state
                    .setValue(HALF, half.get())
                    .setValue(ROTATED, rotated);
        }

        // Wall

        Player player = context.getPlayer();
        boolean prefersHorizontal = player != null && player.isShiftKeyDown();

        // Check what placements are possible
        Optional<DoubleBlockHalf> verticalTopHalf = getPlacementHalf(context, Direction.UP);
        Optional<DoubleBlockHalf> horizontalTopHalf = getPlacementHalf(context, Direction.fromAxisAndDirection(clickedFace.getClockWise().getAxis(), Direction.AxisDirection.POSITIVE));

        if (!verticalTopHalf.isPresent() && !horizontalTopHalf.isPresent()) {
            // Neither is possible
            return null;
        }

        boolean willBeHorizontal = prefersHorizontal && horizontalTopHalf.isPresent() || !verticalTopHalf.isPresent();

        return state
                .setValue(HALF, willBeHorizontal ? horizontalTopHalf.get() : verticalTopHalf.get())
                .setValue(ROTATED, willBeHorizontal);
    }

    @Override
    public boolean lookDirectionInfluencesLocation() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTATED, ACTIVE, HALF);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        // The tile entity exists only on ONE side of the fizzler duo, handling both sides
        return this.isMainBlock(state) && state.getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return this.hasTileEntity(state) ? TileEntityTypeInit.FIZZLER_EMITTER.get().create() : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction upperDirection = getUpperDirection(state);
        Direction facing = state.getValue(FACING);

        if (facing.getAxis().isVertical() && ModUtil.getRotationAmount(rotation) % 2 == 1) {
            state = state.cycle(ROTATED);
        }

        if (upperDirection.getAxisDirection() != rotation.rotate(upperDirection).getAxisDirection()) {
            state = state.cycle(HALF);
        }

        return state.setValue(FACING, rotation.rotate(facing));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction upperDirection = getUpperDirection(state);
        Direction facing = state.getValue(FACING);

        if (mirror.mirror(upperDirection) != upperDirection) {
            state = state.cycle(HALF);
        }

        if (mirror.mirror(facing) != facing) {
            state = state.setValue(FACING, facing.getOpposite());
        }

        return state;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("fizzler_emitter", list);
    }
}
