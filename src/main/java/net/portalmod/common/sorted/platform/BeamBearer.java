package net.portalmod.common.sorted.platform;

import net.portalmod.core.util.StickyBlocks;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

public interface BeamBearer {
    @Nullable
    Direction getBeamDirection(BlockState state);

    default boolean canStickTo(BlockState other) {
        return !(other.getBlock() instanceof BeamBearer) && StickyBlocks.isSticky(other);
    }
}
