package net.portalmod.common.sorted.antline.indicator;

import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class AntlineDecoderBlock extends AntlineIcon implements AntlineActivated, IndicatorActivated {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockReader, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVATED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter blockReader, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVATED) && getConnectedDirection(state) == direction ? 15 : 0;
    }

    @Override
    public void onAntlineActivation(boolean active, BlockState state, Level world, BlockPos pos) {
        // This method gets called on neighbor updates too, so it's safe to check for indicators here

        boolean shouldBeActive = (active || this.checkIndicators(state, world, pos).allIndicatorsActivated);

        if (state.getValue(ACTIVATED) == shouldBeActive)
            return;

        world.setBlock(pos, state.setValue(ACTIVATED, shouldBeActive), 2);
        this.updateNeighbours(world.getBlockState(pos), world, pos);
    }

    @Override
    public boolean ignoreAntlineActivationFromBlock(BlockState state) {
        return state.getBlock() instanceof AntlineEncoderBlock;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.is(newState.getBlock()) && state.getValue(ACTIVATED)) {
            this.updateNeighbours(state, world, pos);
        }
    }

    public void updateNeighbours(BlockState state, Level world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        Direction connectedDirection = getConnectedDirection(state);

        // Update strong redstone power
        world.updateNeighborsAtExceptFromFacing(pos.relative(connectedDirection.getOpposite()), this, connectedDirection);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        return state.getValue(FACE) == AttachFace.FLOOR;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block nBlock, BlockPos nPos, boolean b) {
        this.updateAntlineActivation(state, world, pos);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean b) {
        // First update surrounding antlines, then check for them
        world.updateNeighborsAt(pos, this);
        this.updateAntlineActivation(state, world, pos);
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, Level world, BlockPos pos) {
        Direction.Axis axis = this.getHorsedOn(blockState).getAxis();
        return Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis() != axis)
                .map(pos::relative)
                .collect(Collectors.toList());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("antline_decoder", list);
    }
}