package net.portalmod.common.sorted.panel;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.core.math.Vec3;

/**
 * Interface for blocks to bump a portal into a specific location
 */
public interface PortalHelper {
    boolean containsBlock(BlockState state, BlockPos panelPos, BlockPos pos, World world);
    boolean willHelpPortal(Direction face, Direction horizontalDirection, BlockState state, World world);
    Pair<Vec3, Direction> helpPortal(Vec3 hitPos, Direction face, Direction horizontalDirection, Direction[] lookingDirections, BlockState state, World world);
}
