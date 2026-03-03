package net.portalmod.common.sorted.trigger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class TriggerBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final EnumProperty<TriggerType> TYPE = EnumProperty.create("type", TriggerType.class);
    public static final EnumProperty<TriggerState> STATE = EnumProperty.create("state", TriggerState.class);

    public static final VoxelShape SHAPE_BASE = Block.box(1, 0, 1, 15, 5, 15);
    public static final VoxelShape SHAPE_X = VoxelShapes.or(SHAPE_BASE, Block.box(6, 5, 2, 10, 14, 14));
    public static final VoxelShape SHAPE_Z = VoxelShapes.or(SHAPE_BASE, Block.box(2, 5, 6, 14, 14, 10));

    // TODO
    //  test multiplayer
    //  field visuals for mobs
    //  structure rotations

    public TriggerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(TYPE, TriggerType.PLAYER)
                .setValue(STATE, TriggerState.NULL));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS, TYPE, STATE);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        TileEntity tileEntity = level.getBlockEntity(pos);

        if (!WrenchItem.usedWrench(player, hand) || !(tileEntity instanceof TriggerTileEntity)) {
            return ActionResultType.PASS;
        }

        if (player.isShiftKeyDown()) {
            BlockState cycled = state.cycle(TYPE);
            level.setBlockAndUpdate(pos, cycled);

            WrenchItem.playUseSound(level, rayTraceResult.getLocation());
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.trigger_type." + cycled.getValue(TYPE).getSerializedName()), true);

            return ActionResultType.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) return ActionResultType.PASS;

        TriggerTileEntity trigger = (TriggerTileEntity) tileEntity;
        if (!trigger.isBeingConfigured()) {
            TriggerSelectionServer.startConfiguration((ServerPlayerEntity) player, trigger);
            WrenchItem.playUseSound(level, rayTraceResult.getLocation());
        } else if(trigger.getConfiguringPlayer() != player) {
            WrenchItem.playFailSound(level, rayTraceResult.getLocation());
        }

        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        TileEntity blockEntity = level.getBlockEntity(pos);
        if (state.getValue(STATE) == TriggerState.ACTIVE && blockEntity instanceof TriggerTileEntity) {
            return Math.min(((TriggerTileEntity) blockEntity).getEntityCount(), 15);
        }
        return 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypeInit.TRIGGER.get().create();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("trigger", list);
    }
}