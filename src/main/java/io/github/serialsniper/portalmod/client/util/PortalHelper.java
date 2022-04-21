package io.github.serialsniper.portalmod.client.util;

import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.util.math.*;

import java.util.*;

public class PortalHelper {
    private static final Map<UUID, PortalPair> hierarchy = new HashMap<>();

    public static void add(UUID uuid, PortalEnd end, BlockPos pos) {
        PortalPair pair = new PortalPair();

        if(hierarchy.containsKey(uuid)) {
            pair = hierarchy.get(uuid);

            if(pair.has(end))
                throw new IllegalStateException("MULTIPLE SAME COLOR PORTALS IN THE SAME PAIR");
        }

        pair.set(end, pos);
        hierarchy.put(uuid, pair);
    }

    public static PortalPair get(UUID uuid) {
        return hierarchy.get(uuid);
    }

    public static Map<UUID, PortalPair> getHierarchy() {
        return hierarchy;
    }
}