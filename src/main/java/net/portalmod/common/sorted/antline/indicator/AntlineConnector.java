package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

/**
 * Causes Antlines to connect to this.
 */
public interface AntlineConnector {
    /**
     * Returns the face on which the element is mounted, to allow Antlines to only connect if they are on the same side.
     */
    Direction getHorsedOn(BlockState state);
}
