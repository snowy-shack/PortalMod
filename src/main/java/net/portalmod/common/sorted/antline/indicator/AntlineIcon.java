package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;

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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
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

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ICON);
    }
}
