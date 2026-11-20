package net.portalmod.common.sorted.antline.indicator;

import net.portalmod.common.items.WrenchItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

/**
 * Adds changeable icons to
 */
public abstract class AntlineIcon extends AntlineDevice {
    public static final int ICON_COUNT = 8;

    public static final IntegerProperty ICON = IntegerProperty.create("icon", 0, ICON_COUNT);

    public AntlineIcon(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (WrenchItem.usedWrench(player, hand)) {
            BlockState newState = state.cycle(ICON);

            if (player.isShiftKeyDown()) {
                // Cycle back to the start >:)
                for (int i = 0; i < ICON_COUNT - 1; i++) {
                    newState = newState.cycle(ICON);
                }
            }

            world.setBlockAndUpdate(pos, newState);
            WrenchItem.playUseSound(world, result.getLocation());

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ICON);
    }
}
