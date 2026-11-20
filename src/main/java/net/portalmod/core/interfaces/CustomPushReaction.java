package net.portalmod.core.interfaces;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/** A block that picks its piston push reaction per state (Forge: Block#getPistonPushReaction). */
public interface CustomPushReaction {
    PushReaction getPistonPushReaction(BlockState state);
}
