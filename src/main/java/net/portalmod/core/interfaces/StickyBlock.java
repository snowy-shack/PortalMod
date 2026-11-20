package net.portalmod.core.interfaces;

import net.minecraft.world.level.block.state.BlockState;

/** A block that pistons treat like slime/honey (Forge: IForgeBlock#isStickyBlock / canStickTo). */
public interface StickyBlock {
    boolean isStickyBlock(BlockState state);

    boolean canStickTo(BlockState state, BlockState other);
}
