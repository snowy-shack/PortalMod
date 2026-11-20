package net.portalmod.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A block that redstone dust visually connects to (Forge: Block#canConnectRedstone).
 * Vanilla's check has no world or position, so those are passed as null.
 */
public interface RedstoneConnectable {
    boolean canConnectRedstone(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, @Nullable Direction side);
}
