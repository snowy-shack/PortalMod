package net.portalmod.common.sorted.panel;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.portalmod.core.math.Vec3;

public interface PortalHelper {
    Vec3 helpPortal(Vec3 hitPos, Direction face, BlockState state, World world);
}
