package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineConverterBlock extends AntlineIndicatorBlock  {

    public AntlineConverterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isSignalSource(BlockState p_149744_1_) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader blockReader, BlockPos pos, Direction direction) {
        return this.isActive(state) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader blockReader, BlockPos pos, Direction direction) {
        return this.isActive(state) && getConnectedDirection(state) == direction ? 15 : 0;
    }

    private void updateNeighbours(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        Direction connectedDirection = getConnectedDirection(state);
        world.updateNeighborsAtExceptFromFacing(pos.relative(connectedDirection.getOpposite()), this, connectedDirection);
    }

    @Override
    public void setActive(boolean active, World world, BlockPos pos) {
        super.setActive(active, world, pos);

        this.updateNeighbours(world.getBlockState(pos), world, pos);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.is(newState.getBlock())) {
            if (this.isActive(state)) {
                this.updateNeighbours(state, world, pos);
            }

            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        ActionResultType used = super.use(blockState, world, pos, player, hand, result);
        if (used.consumesAction()) {
            this.updateNeighbours(blockState, world, pos);
        }
        return used;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return state.getValue(FACE) == AttachFace.FLOOR;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_converter", list);
    }
}