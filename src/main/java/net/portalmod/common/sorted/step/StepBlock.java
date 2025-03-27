package net.portalmod.common.sorted.step;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
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
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;

import javax.annotation.Nullable;

public class StepBlock extends Block implements IWaterLoggable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty PILLAR = BooleanProperty.create("pillar");
    public static final BooleanProperty FORCED_PILLAR = BooleanProperty.create("forced_pillar");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StepBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(HALF, Half.BOTTOM)
                .setValue(PILLAR, false)
                .setValue(FORCED_PILLAR, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, PILLAR, FORCED_PILLAR, WATERLOGGED);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (player.getItemInHand(hand).getItem() instanceof StepPillarItem || WrenchItem.usedWrench(player, hand)) {

            if (hasPillarBelow(state, world, pos)) {
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.step.fail"), true);
                return ActionResultType.SUCCESS;
            }

            BlockState cycled = state.cycle(FORCED_PILLAR);

            boolean shouldHavePillar = shouldHavePillar(cycled, world, pos);
            world.setBlockAndUpdate(pos, cycled.setValue(PILLAR, shouldHavePillar));

            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.step." + (shouldHavePillar ? "pillar" : "normal")), true);

            player.playSound(SoundEvents.STONE_PLACE, 1, 0.8f);

            return ActionResultType.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, blockRayTraceResult);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.block(); // TODO add actual shape
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction clickedFace = context.getClickedFace();
        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState()
                .setValue(WATERLOGGED, waterlogged)
                .setValue(FACING, context.getClickedFace());

        boolean bottom;
        if (clickedFace.getAxis() == Direction.Axis.Y && context.getPlayer() != null) {
            // Swap top or bottom when holding shift
            bottom = (clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE) != context.getPlayer().isShiftKeyDown();
        } else {
            bottom = context.getClickLocation().y - context.getClickedPos().getY() < 0.5;
        }

        return state.setValue(HALF, bottom ? Half.BOTTOM : Half.TOP)
                .setValue(PILLAR, hasPillarBelow(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState state, IWorld world, BlockPos pos, BlockPos pos2) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return blockState.setValue(PILLAR, shouldHavePillar(blockState, world, pos));
    }

    public static boolean shouldHavePillar(BlockState blockState, IWorld world, BlockPos pos) {
        return blockState.getValue(FORCED_PILLAR) || hasPillarBelow(blockState, world, pos);
    }

    private static boolean hasPillarBelow(BlockState blockState, IWorld world, BlockPos pos) {
        return world.getBlockState(pos.relative(blockState.getValue(FACING).getOpposite())).getBlock() instanceof StepPillarBlock;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }
}
