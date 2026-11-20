package net.portalmod.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * A block that decides what happens when a player breaks it, e.g. only removing part of it
 * (Forge: Block#removedByPlayer). Return true if the block was removed.
 */
public interface PlayerRemovableBlock {
    boolean removedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid);
}
