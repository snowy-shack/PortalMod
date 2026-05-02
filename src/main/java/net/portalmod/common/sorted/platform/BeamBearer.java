package net.portalmod.common.sorted.platform;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface BeamBearer {
    @Nullable
    Direction getBeamDirection(BlockState state);
}
