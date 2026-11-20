package net.portalmod.core.util;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.portalmod.core.interfaces.StickyBlock;

/** Forge's default sticky-block rules, extended with {@link StickyBlock}. */
public final class StickyBlocks {
    private StickyBlocks() {}

    public static boolean isSticky(BlockState state) {
        if (state.getBlock() instanceof StickyBlock sticky)
            return sticky.isStickyBlock(state);
        return state.is(Blocks.SLIME_BLOCK) || state.is(Blocks.HONEY_BLOCK);
    }

    /** Same as Forge's IForgeBlockState#canStickTo: does {@code state} pull {@code other} along? */
    public static boolean canStickTo(BlockState state, BlockState other) {
        if (state.getBlock() instanceof StickyBlock sticky)
            return sticky.canStickTo(state, other);
        if (state.is(Blocks.HONEY_BLOCK) && other.is(Blocks.SLIME_BLOCK))
            return false;
        if (state.is(Blocks.SLIME_BLOCK) && other.is(Blocks.HONEY_BLOCK))
            return false;
        return isSticky(state) || isSticky(other);
    }
}
