package net.portalmod.common.sorted.panel;

import com.mojang.datafixers.util.Pair;
import net.portalmod.core.math.Vec3;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for blocks to bump a portal into a specific location
 */
public interface PortalHelper {
    boolean containsBlock(BlockState state, BlockPos panelPos, BlockPos pos, Level world);
    boolean willHelpPortal(Direction face, Direction horizontalDirection, BlockState state, Level world);
    Pair<Vec3, Direction> helpPortal(Vec3 hitPos, Direction face, Direction horizontalDirection, Direction[] lookingDirections, BlockState state, Level world);
}
