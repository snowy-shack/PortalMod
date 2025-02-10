package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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

    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty PILLAR = BooleanProperty.create("pillar");
    public static final BooleanProperty FORCED_PILLAR = BooleanProperty.create("forced_pillar");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StepBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, Half.BOTTOM)
                .setValue(PILLAR, false)
                .setValue(FORCED_PILLAR, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF, PILLAR, FORCED_PILLAR, WATERLOGGED);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (WrenchItem.usedWrench(player, hand)) {
            BlockState cycled = state.cycle(FORCED_PILLAR);

            boolean shouldHavePillar = shouldHavePillar(cycled, world, pos);
            world.setBlockAndUpdate(pos, cycled.setValue(PILLAR, shouldHavePillar));

            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.step." + (shouldHavePillar ? "pillar" : "normal")), true);

            WrenchItem.playUseSound(world, player);

            return ActionResultType.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, blockRayTraceResult);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        final double raise = blockState.getValue(HALF) == Half.BOTTOM ? 0 : 8;
        final VoxelShape SHAPE_PLATFORM = Block.box(0, 3 + raise, 0, 16, 8 + raise, 16);
        final VoxelShape SHAPE_PILLAR = Block.box(5, 0, 5, 11, 3 + raise, 11);

        return blockState.getValue(PILLAR) ? VoxelShapes.or(SHAPE_PLATFORM, SHAPE_PILLAR) : SHAPE_PLATFORM;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction clickedFace = context.getClickedFace();
        boolean waterlogged = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        boolean pillar = context.getLevel().getBlockState(context.getClickedPos().below()).getBlock() instanceof StepPillarBlock;

        BlockState state = this.defaultBlockState().setValue(WATERLOGGED, waterlogged).setValue(PILLAR, pillar);

        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return state.setValue(HALF, clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Half.BOTTOM : Half.TOP);
        } else {
            boolean placedOnLowerSide = context.getClickLocation().y - context.getClickedPos().getY() < 0.5;
            return state.setValue(HALF, placedOnLowerSide ? Half.BOTTOM : Half.TOP);
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState state, IWorld world, BlockPos pos, BlockPos pos2) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        boolean hasPillar = shouldHavePillar(blockState, world, pos);
        return blockState.setValue(PILLAR, hasPillar);
    }

    public static boolean shouldHavePillar(BlockState blockState, IWorld world, BlockPos pos) {
        return blockState.getValue(FORCED_PILLAR) || world.getBlockState(pos.below()).getBlock() instanceof StepPillarBlock;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }
}
