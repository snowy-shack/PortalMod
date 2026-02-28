package net.portalmod.common.sorted.trigger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.TileEntityTypeInit;

import javax.annotation.Nullable;

public class TriggerBlock extends Block {
    public static final BooleanProperty ACTIVATED = BlockStateProperties.POWERED;

    public TriggerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(!level.isClientSide) {
            TileEntity tileEntity = level.getBlockEntity(pos);

            if(tileEntity instanceof TriggerTileEntity) {
                TriggerTileEntity trigger = (TriggerTileEntity)tileEntity;

                if(WrenchItem.usedWrench(player, hand)) {
                    if(!trigger.isBeingConfigured()) {
                        trigger.startConfiguration((ServerPlayerEntity)player);
                        WrenchItem.playUseSound(level, rayTraceResult.getLocation());
                    } else {
                        WrenchItem.playFailSound(level, rayTraceResult.getLocation());
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVATED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
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
}