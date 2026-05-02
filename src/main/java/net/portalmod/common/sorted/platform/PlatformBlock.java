package net.portalmod.common.sorted.platform;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.common.blocks.FrameBlock;
import net.portalmod.common.blocks.PortalableBlock;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class PlatformBlock extends BreakableBlock implements IWaterLoggable, PortalableBlock, BeamBearer {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<Half> ORIGINAL_HALF = EnumProperty.create("original_half", Half.class);
    public static final BooleanProperty BEAM = BooleanProperty.create("beam");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PlatformBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(HALF, Half.BOTTOM)
                .setValue(ORIGINAL_HALF, Half.BOTTOM)
                .setValue(BEAM, false)
                .setValue(WATERLOGGED, false)
        );
        this.initAABBs();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, BEAM, ORIGINAL_HALF, WATERLOGGED);
    }

    private static final BiHashMap<Direction, Boolean, VoxelShapeGroup> SHAPES = new BiHashMap<>();

    private void initAABBs() {
        VoxelShapeGroup lowerShape = new VoxelShapeGroup.Builder()
                .add(0, 3, 0, 16, 8, 16)
                .addPart("beam", 5, 0, 5, 11, 3, 11)
                .addPart("beam_collision", 4.5, 0, 4.5, 11.5, 3, 11.5)
                .build();
        VoxelShapeGroup upperShape = new VoxelShapeGroup.Builder()
                .add(0, 11, 0, 16, 16, 16)
                .addPart("beam", 5, 0, 5, 11, 11, 11)
                .addPart("beam_collision", 4.5, 0, 4.5, 11.5, 11, 11.5)
                .build();

        for (Direction facing : Direction.values()) {
            int angleX = facing == Direction.UP
                    ? 0 : facing == Direction.DOWN
                          ? 180 : 90;
            int angleY = facing.get2DDataValue() * 90;

            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));
            matrix.rotateDeg(Vector3f.YN, angleY);
            matrix.rotateDeg(Vector3f.XP, angleX);
            matrix.translate(new Vec3(-.5));

            SHAPES.put(facing, false, lowerShape.clone().transform(matrix));
            SHAPES.put(facing, true, upperShape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.getValue(FACING), state.getValue(HALF) == Half.TOP)
                .getVariant(state.getValue(BEAM) ? "beam" : "");
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.getValue(FACING), state.getValue(HALF) == Half.TOP)
                .getVariant(state.getValue(BEAM) ? "beam_collision" : "");
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (!PlatformBeamBlock.isBeamItem(player.getItemInHand(hand).getItem())) {
            return ActionResultType.PASS;
        }

        if (hasBeamBelow(state, world, pos)) {
            return ActionResultType.FAIL;
        }

        BlockState cycled = state.cycle(BEAM);
        world.setBlockAndUpdate(pos, cycled);

        player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.platform." + (cycled.getValue(BEAM) ? "beam" : "normal")), true);

        player.playSound(SoundEvents.STONE_PLACE, 1, 0.8f * ModUtil.randomSlightSoundPitch());

        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;

        BlockState state = this.defaultBlockState()
                .setValue(WATERLOGGED, waterlogged)
                .setValue(FACING, this.getPlacementDirection(context))
                .setValue(HALF, context.getPlayer().isShiftKeyDown() ? Half.BOTTOM : Half.TOP)
                .setValue(ORIGINAL_HALF, context.getPlayer().isShiftKeyDown() ? Half.BOTTOM : Half.TOP);

        return state.setValue(BEAM, hasBeamBelow(state, context.getLevel(), context.getClickedPos()));
    }

    public Direction getPlacementDirection(BlockItemUseContext context) {
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        Block block = clickedState.getBlock();

        // Align direction when placed next to each other
        if (block instanceof PlatformBlock) {
            Direction facing = clickedState.getValue(PlatformBlock.FACING);
            if (facing.getAxis() != context.getClickedFace().getAxis()) {
                return facing;
            }
        }

        // Align with beam
        if (block instanceof BeamBearer) {
            Direction beamDirection = ((BeamBearer) block).getBeamDirection(clickedState);
            if (beamDirection != null && beamDirection.getAxis() == context.getClickedFace().getAxis()) {
                return context.getClickedFace();
            }
        }

        return context.getNearestLookingDirection().getOpposite();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        if (direction == state.getValue(FACING).getOpposite() && hasBeamBelow(state, world, pos)) {
            return state.setValue(BEAM, true);
        }

        return state;
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean b) {
        // Check for power to immediately update the state
        world.neighborChanged(pos, this, pos);
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        boolean power = level.hasNeighborSignal(pos);

        Half oldHalf = state.getValue(HALF);
        Half newHalf = power ? Half.TOP : state.getValue(ORIGINAL_HALF);

        if (newHalf != oldHalf) {
            level.setBlockAndUpdate(pos, state.setValue(HALF, newHalf));
        }
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    public static boolean hasBeamBelow(BlockState blockState, IWorld world, BlockPos pos) {
        BlockState belowState = world.getBlockState(pos.relative(blockState.getValue(FACING).getOpposite()));
        Block block = belowState.getBlock();

        if (!(block instanceof PlatformBeamBlock || block instanceof FrameBlock)) return false;

        Direction beamDirection = ((BeamBearer) block).getBeamDirection(belowState);
        return beamDirection != null && beamDirection.getAxis() == blockState.getValue(FACING).getAxis();
    }

    @Nullable
    @Override
    public Direction getBeamDirection(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("platform", list);
    }

    @Override
    public boolean isPortalableOnFace(BlockState state, Direction face) {
        return state.getValue(FACING) == face;
    }
}
