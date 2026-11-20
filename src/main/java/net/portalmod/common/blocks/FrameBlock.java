package net.portalmod.common.blocks;

import com.mojang.math.Axis;
import net.portalmod.common.sorted.platform.BeamBearer;
import net.portalmod.common.sorted.platform.PlatformBeamBlock;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class FrameBlock extends Block implements SimpleWaterloggedBlock, CustomPushBehavior, BeamBearer {
    public static Set<Block> FRAME_BLOCKS = new HashSet<>();

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty BEAM = BooleanProperty.create("beam");

    public boolean isFilled;

    public FrameBlock(Properties properties, boolean isFilled) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(BEAM, false)
                .setValue(WATERLOGGED, false));
        this.initAABBs();
        this.isFilled = isFilled;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BEAM, WATERLOGGED);
    }

    private static final Map<Direction, VoxelShapeGroup> FILLED_SHAPE = new HashMap<>();
    private static final Map<Direction, VoxelShapeGroup> HOLLOW_SHAPE = new HashMap<>();

    private void initAABBs() {
        VoxelShapeGroup filledShape = new VoxelShapeGroup.Builder()
                .add(0, 0, 0, 2, 16, 16)
                .addPart("beam", 0, 5, 5, 16, 11, 11)
                .addPart("beam_collision", 0, 4.5, 4.5, 16, 11.5, 11.5)
                .build();
        VoxelShapeGroup hollowShape = new VoxelShapeGroup.Builder()
                .add(0,0, 0, 2, 1,16)
                .add(0,15,0, 2,16,16)
                .add(0,1, 0, 2,15, 1)
                .add(0,1, 15,2,15,16)
                .addPart("beam", 0, 5, 5, 16, 11, 11)
                .addPart("beam_collision", 0, 4.5, 4.5, 16, 11.5, 11.5)
                .build();

        for(Direction facing : Direction.values()) {
            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));

            if(facing.getAxis() == Direction.Axis.Y) {
                int angle = (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ? 90 : -90;
                matrix.rotateDeg(Axis.ZP, angle);
            } else {
                int angle = facing.get2DDataValue() * -90 + 90;
                matrix.rotateDeg(Axis.YP, angle);
            }

            matrix.translate(new Vec3(-.5));

            FILLED_SHAPE.put(facing, filledShape.clone().transform(matrix));
            HOLLOW_SHAPE.put(facing, hollowShape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (FRAME_BLOCKS.isEmpty()) {
            DefaultedRegistry.BLOCK.stream().filter(this::holdingCausesFullHitbox).forEach(block -> FRAME_BLOCKS.add(block));
        }

        // Sadly we can't access the held item itself, we can only test whether a particular item is held.
        boolean holdingFrame = FRAME_BLOCKS.stream().anyMatch(block -> context.isHoldingItem(block.asItem()));

        boolean hasBeam = state.getValue(BEAM);
        return (this.isFilled || holdingFrame ? FILLED_SHAPE : HOLLOW_SHAPE).get(state.getValue(FACING)).getVariant(hasBeam ? "beam" : "");
    }

    public boolean holdingCausesFullHitbox(Block block) {
        return block instanceof FrameBlock || block instanceof PlatformBeamBlock;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean hasBeam = state.getValue(BEAM);
        return (this.isFilled ? FILLED_SHAPE : HOLLOW_SHAPE).get(state.getValue(FACING)).getVariant(hasBeam ? "beam_collision" : "");
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        if (!PlatformBeamBlock.isBeamItem(player.getItemInHand(hand).getItem())) {
            return InteractionResult.PASS;
        }

        BlockState cycled = state.cycle(BEAM);
        world.setBlockAndUpdate(pos, cycled);

        player.displayClientMessage(Component.translatable("actionbar.portalmod.iron_frame." + (cycled.getValue(BEAM) ? "beam" : "normal")), true);

        player.playSound(SoundEvents.STONE_PLACE, 1, 0.8f * ModUtil.randomSlightSoundPitch());

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        Player player = context.getPlayer();
        return this.defaultBlockState()
                .setValue(FACING, player != null && player.isShiftKeyDown() ? nearestLookingDirection.getOpposite() : nearestLookingDirection)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Nullable
    @Override
    public Direction getBeamDirection(BlockState state) {
        return state.getValue(BEAM) ? state.getValue(FACING) : null;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        return this.canStickTo(other);
    }

    @Override
    public boolean isStickyToNeighbor(Level level, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction dir, Direction moveDir) {
        return state.getValue(BEAM) && state.getValue(FACING).getAxis() == dir.getAxis();
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
        ModUtil.addTooltip("frame", list);
    }
}
