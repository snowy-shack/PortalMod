package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.common.sorted.platform.BeamBearer;
import net.portalmod.common.sorted.platform.PlatformBeamBlock;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.*;

public class FrameBlock extends Block implements IWaterLoggable, BeamBearer {
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
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
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
                matrix.rotateDeg(Vector3f.ZP, angle);
            } else {
                int angle = facing.get2DDataValue() * -90 + 90;
                matrix.rotateDeg(Vector3f.YP, angle);
            }

            matrix.translate(new Vec3(-.5));

            FILLED_SHAPE.put(facing, filledShape.clone().transform(matrix));
            HOLLOW_SHAPE.put(facing, hollowShape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
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
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        boolean hasBeam = state.getValue(BEAM);
        return (this.isFilled ? FILLED_SHAPE : HOLLOW_SHAPE).get(state.getValue(FACING)).getVariant(hasBeam ? "beam_collision" : "");
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (!PlatformBeamBlock.isBeamItem(player.getItemInHand(hand).getItem())) {
            return ActionResultType.PASS;
        }

        BlockState cycled = state.cycle(BEAM);
        world.setBlockAndUpdate(pos, cycled);

        player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.iron_frame." + (cycled.getValue(BEAM) ? "beam" : "normal")), true);

        player.playSound(SoundEvents.STONE_PLACE, 1, 0.8f * ModUtil.randomSlightSoundPitch());

        return ActionResultType.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        PlayerEntity player = context.getPlayer();
        return this.defaultBlockState()
                .setValue(FACING, player != null && player.isShiftKeyDown() ? nearestLookingDirection.getOpposite() : nearestLookingDirection)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
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
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("frame", list);
    }
}
