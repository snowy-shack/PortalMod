package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;

/**
 * Activates Antlines.
 */
public interface AntlineActivator extends AntlineConnector {
    boolean isAntlineActive(BlockState state);
}
