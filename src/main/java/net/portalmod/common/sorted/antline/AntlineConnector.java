package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface AntlineConnector {
    Direction getHorsedOn(BlockState state);
}
