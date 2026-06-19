package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

/**
 * Activates Antlines.
 */
public interface AntlineActivator extends AntlineConnector {
    boolean isAntlineActive(BlockState state);

    default boolean isAntlineActive(BlockState state, Direction side, Direction connection) {
        return isAntlineActive(state);
    }
}
