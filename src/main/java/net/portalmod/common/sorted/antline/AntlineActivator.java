package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;

public interface AntlineActivator extends AntlineConnector {
    boolean isActive(BlockState state);
}
