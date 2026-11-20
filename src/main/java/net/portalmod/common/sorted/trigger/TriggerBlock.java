package net.portalmod.common.sorted.trigger;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.EntityBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class TriggerBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final EnumProperty<TriggerType> TYPE = EnumProperty.create("type", TriggerType.class);
    public static final EnumProperty<TriggerState> STATE = EnumProperty.create("state", TriggerState.class);

    public static final VoxelShape SHAPE_BASE = Block.box(1, 0, 1, 15, 5, 15);
    public static final VoxelShape SHAPE_X = Shapes.or(SHAPE_BASE, Block.box(6, 5, 2, 10, 14, 14));
    public static final VoxelShape SHAPE_Z = Shapes.or(SHAPE_BASE, Block.box(2, 5, 6, 14, 14, 10));

    // TODO
    //  test multiplayer

    public TriggerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(TYPE, TriggerType.PLAYER)
                .setValue(STATE, TriggerState.NULL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, TYPE, STATE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        BlockEntity tileEntity = level.getBlockEntity(pos);

        if (!WrenchItem.usedWrench(player, hand) || !(tileEntity instanceof TriggerTileEntity)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            BlockState cycled = state.cycle(TYPE);
            level.setBlockAndUpdate(pos, cycled);

            WrenchItem.playUseSound(level, rayTraceResult.getLocation());
            player.displayClientMessage(Component.translatable("actionbar.portalmod.trigger_type." + cycled.getValue(TYPE).getSerializedName()), true);

            if (tileEntity instanceof TriggerTileEntity) ((TriggerTileEntity) tileEntity).updateTriggerType();

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) return InteractionResult.PASS;

        TriggerTileEntity trigger = (TriggerTileEntity) tileEntity;
        if (!trigger.isBeingConfigured()) {
            TriggerSelectionServer.startConfiguration((ServerPlayer) player, trigger);
            WrenchItem.playUseSound(level, rayTraceResult.getLocation());
        } else if(trigger.getConfiguringPlayer() != player) {
            WrenchItem.playFailSound(level, rayTraceResult.getLocation());
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (state.getValue(STATE) == TriggerState.ACTIVE && blockEntity instanceof TriggerTileEntity) {
            return Math.min(((TriggerTileEntity) blockEntity).getEntityCount(), 15);
        }
        return 0;
    }

    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TileEntityTypeInit.TRIGGER.get().create(pos, state);
    }

    // 1.17+: block entities tick through a ticker the block hands out (was TickableBlockEntity).
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypeInit.TRIGGER.get() ? (lvl, pos, st, be) -> ((TriggerTileEntity) be).tick() : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return ModUtil.getRotationAmount(rotation) % 2 == 1 ? state.cycle(AXIS) : state;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("trigger", list);
    }
}