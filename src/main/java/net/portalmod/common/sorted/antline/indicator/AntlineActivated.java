package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for things that are powered by antlines, such as indicators.
 */
public interface AntlineActivated extends AntlineConnector {
    void setActive(boolean active, World world, BlockPos pos);
}
