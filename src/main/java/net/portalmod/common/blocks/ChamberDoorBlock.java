package net.portalmod.common.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberDoorBlock extends Block {
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

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
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
    public void playerWillDestroy(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
        if (!world.isClientSide) {
            if (player.isCreative()) {
                preventCreativeDropFromBottomPart(world, pos, blockState, player);
            } else {
                dropResources(blockState, world, pos, null, player, player.getMainHandItem());
            }
        }

        super.playerWillDestroy(world, pos, blockState, player);
    }

    @Override
    public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState blockState, @Nullable TileEntity tileEntity, ItemStack itemStack) {
        super.playerDestroy(world, player, pos, Blocks.AIR.defaultBlockState(), tileEntity, itemStack);
    }

    protected static void preventCreativeDropFromBottomPart(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
        DoubleBlockHalf doubleblockhalf = blockState.getValue(HALF);
        Side side = blockState.getValue(SIDE);
        if (doubleblockhalf == DoubleBlockHalf.UPPER || side == Side.RIGHT) {
            for (BlockPos otherPos : getOtherPositions(blockState, pos)) {
                BlockState blockstate = world.getBlockState(otherPos);
                if (blockstate.getBlock() == blockState.getBlock() && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER && blockstate.getValue(SIDE) == Side.LEFT) {
                    world.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                    world.levelEvent(player, 2001, otherPos, Block.getId(blockstate));
                }
            }
        }
    }

    public static void setValue(BooleanProperty property, boolean open, BlockState blockState, World world, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(SIDE) == Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();

        world.setBlock(pos, world.getBlockState(pos).setValue(property, open), 2);
        world.setBlock(pos.relative(horizontal), world.getBlockState(pos.relative(horizontal)).setValue(property, open), 2);
        world.setBlock(pos.relative(vertical), world.getBlockState(pos.relative(vertical)).setValue(property, open), 2);
        world.setBlock(pos.relative(horizontal).relative(vertical), world.getBlockState(pos.relative(horizontal).relative(vertical)).setValue(property, open), 2);
    }

    public static void setOpen(boolean open, BlockState blockState, World world, BlockPos pos) {
        setValue(OPEN, open, blockState, world, pos);
    }

    public static void setPowered(boolean open, BlockState blockState, World world, BlockPos pos) {
        setValue(POWERED, open, blockState, world, pos);
    }

    private static void playSound(boolean open, World world, BlockPos pos) {
        world.playSound(null, pos, open ? SoundInit.CHAMBER_DOOR_OPEN.get() : SoundInit.CHAMBER_DOOR_CLOSE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity p_225533_4_, Hand hand, BlockRayTraceResult p_225533_6_) {
        boolean open = !blockState.getValue(OPEN);
        setOpen(open, blockState, world, pos);
        playSound(open, world, pos);
        return ActionResultType.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction p_196271_2_, BlockState p_196271_3_, IWorld world, BlockPos pos, BlockPos p_196271_6_) {
        Direction facing = blockState.getValue(FACING);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(SIDE) == Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();

        if (isBlockAt(pos.relative(vertical), (World) world) && isBlockAt(pos.relative(horizontal), (World) world) && isBlockAt(pos.relative(vertical).relative(horizontal), (World) world)) {
            return blockState;
        }
        return Blocks.AIR.defaultBlockState();
    }

    public boolean isBlockAt(BlockPos pos, World world) {
        return world.getBlockState(pos).is(this);
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
            if (blockState.getValue(OPEN) != isPowered) {
                setOpen(isPowered, blockState, world, pos);
                if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER && blockState.getValue(SIDE) == Side.LEFT) {
                    playSound(isPowered, world, pos);
                }
            }
        }

        Direction facing = blockState.getValue(FACING);

        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(blockState, pos));
        possibleIndicatorPositions.addAll(getSurroundingPositions(blockState, pos.relative(facing)));
        possibleIndicatorPositions.addAll(getSurroundingPositions(blockState, pos.relative(facing.getOpposite())));

        boolean hasIndicators = false;
        boolean allIndicatorsActivated = true;

        for (BlockPos blockPos : possibleIndicatorPositions) {
            BlockState worldBlockState = world.getBlockState(blockPos);
            if (worldBlockState.getBlock() instanceof AntlineIndicatorBlock) {
                hasIndicators = true;
                if (!worldBlockState.getValue(AntlineIndicatorBlock.ACTIVE)) {
                    allIndicatorsActivated = false;
                }
            }
        }

        if (hasIndicators && wasPowered != allIndicatorsActivated) {
            setPowered(allIndicatorsActivated, blockState, world, pos);
            if (blockState.getValue(OPEN) != allIndicatorsActivated) {
                setOpen(allIndicatorsActivated, blockState, world, pos);
                playSound(isPowered, world, pos);
            }
        }
    }

    public static List<BlockPos> getOtherPositions(BlockState blockState, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(SIDE) == Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();

        return new ArrayList<>(Arrays.asList(
                pos.relative(vertical),
                pos.relative(horizontal),
                pos.relative(vertical).relative(horizontal)));
    }

    public static List<BlockPos> getAllPositions(BlockState blockState, BlockPos pos) {
        List<BlockPos> otherPositions = getOtherPositions(blockState, pos);
        otherPositions.add(pos);
        return otherPositions;
    }

    /*
    horizontal >
    vertical ^

    5      6       7      8
    4     door    door    9
    3   updated   door    10
    2      1       12     11

     */
    public static List<BlockPos> getSurroundingPositions(BlockState blockState, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(SIDE) == Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();

        return new ArrayList<>(Arrays.asList(
                pos.relative(vertical.getOpposite()),
                pos.relative(horizontal.getOpposite()).relative(vertical.getOpposite()),
                pos.relative(horizontal.getOpposite()),
                pos.relative(horizontal.getOpposite()).relative(vertical),
                pos.relative(horizontal.getOpposite()).relative(vertical, 2),
                pos.relative(vertical, 2),
                pos.relative(horizontal).relative(vertical, 2),
                pos.relative(horizontal, 2).relative(vertical, 2),
                pos.relative(horizontal, 2).relative(vertical),
                pos.relative(horizontal, 2),
                pos.relative(horizontal, 2).relative(vertical.getOpposite()),
                pos.relative(horizontal).relative(vertical.getOpposite())
        ));
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