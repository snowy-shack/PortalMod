package net.portalmod.common.sorted.panel;

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
    boolean willHelpPortal(Direction face, BlockState state, World world);
    Vec3 helpPortal(Vec3 hitPos, Direction face, BlockState state, World world);
}
