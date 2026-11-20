package net.portalmod.common.sorted.portal;

import net.portalmod.common.sorted.portalgun.PortalHelperServerManager;
import net.portalmod.core.math.Vec3;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Used to add a temporary portal helper for the following tick
 */
public class VolatilePortalHelperManager {
    private static VolatilePortalHelperManager instance;

    private final Map<ResourceKey<Level>, List<VolatilePortalHelper>> volatilePortalHelpers;

    private VolatilePortalHelperManager() {
        this.volatilePortalHelpers = new HashMap<>();
    }

    public static VolatilePortalHelperManager getInstance() {
        if(instance == null)
            instance = new VolatilePortalHelperManager();
        return instance;
    }

    public void addVolatilePortalHelper(Level level, Vec3 position, Direction normal, float radius) {
        List<VolatilePortalHelper> helpers = this.volatilePortalHelpers.getOrDefault(level.dimension(), new ArrayList<>());
        helpers.add(new VolatilePortalHelper(position, normal, radius));
        this.volatilePortalHelpers.put(level.dimension(), helpers);
    }

    public void clearVolatilePortalHelpers() {
        this.volatilePortalHelpers.forEach((k, v) -> v.clear());
    }

    public Optional<VolatilePortalHelper> findHelperThatWillHelp(ServerPlayer player, UUID gun, PortalEnd end, Level level, Vec3 hitPos, Direction face) {
        List<VolatilePortalHelper> helpers = new ArrayList<>(this.volatilePortalHelpers.getOrDefault(level.dimension(), new ArrayList<>()));

        helpers = helpers.stream().sorted((o1, o2) ->
                (int)Math.signum(o1.position.clone().sub(hitPos).magnitudeSqr() - o2.position.clone().sub(hitPos).magnitudeSqr()))
                .collect(Collectors.toList());

        for(VolatilePortalHelper helper : helpers)
            if(PortalHelperServerManager.getInstance().willBeHelped(player, gun, end, hitPos, face, helper))
                return Optional.of(helper);
        return Optional.empty();
    }
}