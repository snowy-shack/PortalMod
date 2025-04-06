package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.portalmod.common.sorted.antline.indicator.AntlineConnector;

/**
 * Activates Antlines.
 */
public interface AntlineActivator extends AntlineConnector {
    boolean isActive(BlockState state);
}
