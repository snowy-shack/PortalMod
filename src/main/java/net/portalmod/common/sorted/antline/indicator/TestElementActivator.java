package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Testing elements get activated by this interface.
 */
public interface TestElementActivator {
    boolean isActive(BlockState state);
}
