package net.portalmod.common.sorted.antline.indicator;

import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

/**
 * Abstract class for antline devices that can activate testing elements and can be reversed, such as {@link AntlineIndicatorBlock} and {@link AntlineTimerBlock}.
 */
public abstract class AntlineOutput extends AntlineDevice implements AntlineActivated, TestElementActivator {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");

    public AntlineOutput(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED, REVERSED);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        boolean reversed = blockState.getValue(REVERSED);
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
            world.setBlockAndUpdate(pos, blockState.setValue(REVERSED, !reversed));
            player.displayClientMessage(new TranslatableComponent("actionbar.portalmod.indicator_mode." + (reversed ? "normal" : "reversed")), true);
            WrenchItem.playUseSound(world, result.getLocation());

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public void playActivationSound(boolean active, Level world, BlockPos pos) {
        world.playSound(null, pos, (active ? SoundInit.ANTLINE_INDICATOR_ACTIVATE : SoundInit.ANTLINE_INDICATOR_DEACTIVATE).get(), SoundSource.BLOCKS, 3, ModUtil.randomSlightSoundPitch());
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block nBlock, BlockPos nPos, boolean b) {
        this.updateAntlineActivation(state, world, pos);

        super.neighborChanged(state, world, pos, nBlock, nPos, b);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean b) {
        // First update surrounding antlines, then check for them
        world.updateNeighborsAt(pos, this);
        this.updateAntlineActivation(state, world, pos);
    }
}
