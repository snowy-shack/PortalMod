package net.portalmod.common.sorted.antline;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Activates Antlines.
 */
public interface AntlineActivator extends AntlineConnector {
    boolean isAntlineActive(BlockState state);
}
