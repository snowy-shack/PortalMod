package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.init.SoundInit;

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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED, REVERSED);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        boolean reversed = blockState.getValue(REVERSED);
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
            world.setBlockAndUpdate(pos, blockState.setValue(REVERSED, !reversed));
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.indicator_mode." + (reversed ? "normal" : "reversed")), true);
            WrenchItem.playUseSound(world, result.getLocation());

            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public void playActivationSound(boolean active, World world, BlockPos pos) {
        world.playSound(null, pos, (active ? SoundInit.ANTLINE_INDICATOR_ACTIVATE : SoundInit.ANTLINE_INDICATOR_DEACTIVATE).get(), SoundCategory.BLOCKS, 3, 1);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block nBlock, BlockPos nPos, boolean b) {
        this.updatePower(state, world, pos);

        super.neighborChanged(state, world, pos, nBlock, nPos, b);
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean b) {
        // First update surrounding antlines, then check for them
        world.updateNeighborsAt(pos, this);
        this.updatePower(state, world, pos);
    }
}
