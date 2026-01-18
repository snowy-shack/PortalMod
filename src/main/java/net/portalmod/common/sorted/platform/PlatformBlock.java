package net.portalmod.common.sorted.platform;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
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
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class PlatformBlock extends BreakableBlock implements IWaterLoggable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<Half> ORIGINAL_HALF = EnumProperty.create("original_half", Half.class);
    public static final BooleanProperty BEAM = BooleanProperty.create("beam");
    public static final BooleanProperty FORCED_BEAM = BooleanProperty.create("forced_beam");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PlatformBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(HALF, Half.BOTTOM)
                .setValue(ORIGINAL_HALF, Half.BOTTOM)
                .setValue(BEAM, false)
                .setValue(FORCED_BEAM, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, BEAM, ORIGINAL_HALF, FORCED_BEAM, WATERLOGGED);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (isBeamItem(player.getItemInHand(hand).getItem()) || WrenchItem.usedWrench(player, hand)) {

            if (hasBeamBelow(state, world, pos)) {
                return ActionResultType.FAIL;
            }

            BlockState cycled = state.cycle(FORCED_BEAM);

            boolean shouldHaveBeam = shouldHaveBeam(cycled, world, pos);
            world.setBlockAndUpdate(pos, cycled.setValue(BEAM, shouldHaveBeam));

            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.platform." + (shouldHaveBeam ? "beam" : "normal")), true);

            player.playSound(SoundEvents.STONE_PLACE, 1, 0.8f);

            return ActionResultType.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, blockRayTraceResult);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        double raise = blockState.getValue(HALF) == Half.BOTTOM ? 0 : 8;
        VoxelShapeGroup SHAPE_PLATFORM = new VoxelShapeGroup.Builder()
                .add(0, 3 + raise, 0, 16, 8 + raise, 16)
                .build();
        VoxelShapeGroup SHAPE_BEAM = new VoxelShapeGroup.Builder()
                .add(5, 0, 5, 11, 3 + raise, 11)
                .add(0, 3 + raise, 0, 16, 8 + raise, 16)
                .build();

        VoxelShapeGroup combined = blockState.getValue(BEAM) ? SHAPE_BEAM : SHAPE_PLATFORM;

        // Rotate according to FACING
        int angleX = blockState.getValue(FACING) == Direction.UP
                ? 0 : blockState.getValue(FACING) == Direction.DOWN
                    ? 180 : 90;
        int angleY = blockState.getValue(FACING).get2DDataValue() * 90;

        Mat4 matrix = Mat4.identity();
        matrix.translate(new Vec3(.5));
        matrix.rotateDeg(Vector3f.YN, angleY);
        matrix.rotateDeg(Vector3f.XP, angleX);
        matrix.translate(new Vec3(-.5));

        return combined.transform(matrix).getShape();
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

        // Align direction when placed next to each other
        if (clickedState.getBlock() instanceof PlatformBlock) {
            Direction facing = clickedState.getValue(PlatformBlock.FACING);
            if (facing.getAxis() != context.getClickedFace().getAxis()) {
                return facing;
            }
        }

        return context.getClickedFace();
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState state, IWorld world, BlockPos pos, BlockPos pos2) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        BlockState newState = blockState.setValue(BEAM, shouldHaveBeam(blockState, world, pos));

        return newState;
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        super.neighborChanged(state, level, pos, block, neighborPos, b);

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

    public static boolean shouldHaveBeam(BlockState blockState, IWorld world, BlockPos pos) {
        return blockState.getValue(FORCED_BEAM) || hasBeamBelow(blockState, world, pos);
    }

    public static boolean hasBeamBelow(BlockState blockState, IWorld world, BlockPos pos) {
        BlockState belowState = world.getBlockState(pos.relative(blockState.getValue(FACING).getOpposite()));

        return (belowState.getBlock() instanceof PlatformBeamBlock
                && belowState.getValue(FACING).getAxis() == blockState.getValue(FACING).getAxis());
    }

    public static boolean isBeamItem(Item item) {
        return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof PlatformBeamBlock;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("platform", list);
    }
}
