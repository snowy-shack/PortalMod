package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface AntlineActivator {
    boolean isActive(BlockState state);
    Direction getHorsedOn(BlockState state);
}
