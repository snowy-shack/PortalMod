package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for things that are powered by antlines, such as indicators.
 */
public interface AntlineOutput extends AntlineConnector {
    boolean isActive(BlockState blockState);
    void setActive(boolean active, World world, BlockPos pos);
}
