package net.portalmod.common.sorted.platform;

import net.portalmod.common.blocks.FrameBlock;
import net.portalmod.common.blocks.PortalableBlock;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class PlatformBlock extends HalfTransparentBlock implements SimpleWaterloggedBlock, PortalableBlock, BeamBearer {

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING), state.getValue(HALF) == Half.TOP)
                .getVariant(state.getValue(BEAM) ? "beam" : "");
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING), state.getValue(HALF) == Half.TOP)
                .getVariant(state.getValue(BEAM) ? "beam_collision" : "");
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        if (!PlatformBeamBlock.isBeamItem(player.getItemInHand(hand).getItem())) {
            return InteractionResult.PASS;
        }

        if (hasBeamBelow(state, world, pos)) {
            return InteractionResult.FAIL;
        }

        BlockState cycled = state.cycle(BEAM);
        world.setBlockAndUpdate(pos, cycled);

        player.displayClientMessage(new TranslatableComponent("actionbar.portalmod.platform." + (cycled.getValue(BEAM) ? "beam" : "normal")), true);

        player.playSound(SoundEvents.STONE_PLACE, 1, 0.8f * ModUtil.randomSlightSoundPitch());

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;

        BlockState state = this.defaultBlockState()
                .setValue(WATERLOGGED, waterlogged)
                .setValue(FACING, this.getPlacementDirection(context))
                .setValue(HALF, context.getPlayer().isShiftKeyDown() ? Half.BOTTOM : Half.TOP)
                .setValue(ORIGINAL_HALF, context.getPlayer().isShiftKeyDown() ? Half.BOTTOM : Half.TOP);

        return state.setValue(BEAM, hasBeamBelow(state, context.getLevel(), context.getClickedPos()));
    }

    public Direction getPlacementDirection(BlockPlaceContext context) {
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        if (direction == state.getValue(FACING).getOpposite() && hasBeamBelow(state, world, pos)) {
            return state.setValue(BEAM, true);
        }

        return state;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean b) {
        // Check for power to immediately update the state
        world.neighborChanged(pos, this, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
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

    public static boolean hasBeamBelow(BlockState blockState, LevelAccessor world, BlockPos pos) {
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
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("platform", list);
    }

    @Override
    public boolean isPortalableOnFace(BlockState state, Direction face) {
        return state.getValue(FACING) == face;
    }
}
