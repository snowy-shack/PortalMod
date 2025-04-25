package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

/**
 * Causes Antlines to connect to this.
 */
public interface AntlineConnector {
    /**
     * Returns the direction of the surface on which the element is mounted, to allow Antlines to only connect if they are on the same side.
     */
    Direction getHorsedOn(BlockState state);

    boolean connectsInDirection(Direction direction, BlockState state);
}
