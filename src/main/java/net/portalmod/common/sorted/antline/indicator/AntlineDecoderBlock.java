package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineDecoderBlock extends AntlineIcon implements AntlineActivated {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public AntlineDecoderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
                .setValue(ICON, 0)
                .setValue(ACTIVATED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader blockReader, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVATED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader blockReader, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVATED) && getConnectedDirection(state) == direction ? 15 : 0;
    }

    @Override
    public void setActive(boolean active, BlockState state, World world, BlockPos pos) {
        if (state.getValue(ACTIVATED) == active) return;

        world.setBlock(pos, state.setValue(ACTIVATED, active), 2);
        this.updateNeighbours(world.getBlockState(pos), world, pos);
    }

    @Override
    public boolean ignoreActivationFromBlock(BlockState state) {
        return state.getBlock() instanceof AntlineEncoderBlock;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.is(newState.getBlock()) && state.getValue(ACTIVATED)) {
            this.updateNeighbours(state, world, pos);
        }
    }

    public void updateNeighbours(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        Direction connectedDirection = getConnectedDirection(state);

        // Update strong redstone power
        world.updateNeighborsAtExceptFromFacing(pos.relative(connectedDirection.getOpposite()), this, connectedDirection);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return state.getValue(FACE) == AttachFace.FLOOR;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block nBlock, BlockPos nPos, boolean b) {
        this.updatePower(state, world, pos);
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean b) {
        // First update surrounding antlines, then check for them
        world.updateNeighborsAt(pos, this);
        this.updatePower(state, world, pos);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_decoder", list);
    }
}