package net.portalmod.common.sorted.panel;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.math.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for blocks to bump a portal into a specific location
 */
public interface PortalHelper {
    Map<UUID, Map<PortalEnd, Long>> LAST_USED_TICKS = new HashMap<>();
    int COOLDOWN_TICKS = 15;

    static void updateLastUsed(UUID gunUUID, PortalEnd end) {
        long currentTick = ServerLifecycleHooks.getCurrentServer().getTickCount();
        LAST_USED_TICKS.computeIfAbsent(gunUUID, k -> new HashMap<>()).put(end, currentTick);
    }

    static long getTicksSinceUsed(UUID gunUUID, PortalEnd end) {
        if (!LAST_USED_TICKS.containsKey(gunUUID) || !LAST_USED_TICKS.get(gunUUID).containsKey(end)) {
            return Long.MAX_VALUE;
        }
        long placedTick = LAST_USED_TICKS.get(gunUUID).get(end);
        return ServerLifecycleHooks.getCurrentServer().getTickCount() - placedTick;
    }

    static boolean isCooldown(UUID gunUUID, PortalEnd end) {
        return PortalHelper.getTicksSinceUsed(gunUUID, end) > COOLDOWN_TICKS;
    }

    Vec3 helpPortal(Vec3 hitPos, Direction face, BlockState state, World world);
}
