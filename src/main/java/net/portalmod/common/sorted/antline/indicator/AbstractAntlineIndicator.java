package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivator;
import net.portalmod.common.sorted.antline.AntlineBlock;

/**
 * Defines a format for regular indicators and handles the reversed property.
 */
public abstract class AbstractAntlineIndicator extends AntlineDevice implements AntlineActivated, TestElementActivator {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");

    public AbstractAntlineIndicator(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        boolean reversed = blockState.getValue(REVERSED);
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
            world.setBlockAndUpdate(pos, blockState.setValue(REVERSED, !reversed));
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.indicator_mode." + (reversed ? "normal" : "reversed")), true);

            WrenchItem.playUseSound(world, player);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block nBlock, BlockPos nPos, boolean b) {
        super.neighborChanged(state, world, pos, nBlock, nPos, b);

        // Check other blocks for power, like buttons
        for (Direction direction : Direction.values()) {
            BlockState blockState = world.getBlockState(pos.relative(direction));
            Block neighborBlock = blockState.getBlock();

            // If a neighbor is antline, only listen to antline
            if (neighborBlock instanceof AntlineBlock) {
                return;
            }

            if (neighborBlock instanceof AntlineActivator && ((AntlineActivator) neighborBlock).isActive(blockState)) {
                this.setActive(true, world, pos);
                return;
            }
        }

        this.setActive(false, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED, REVERSED);
    }
}
