package net.portalmod.common.sorted.portal;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPortalManager {
    private static ClientPortalManager instance;
    private final Map<UUID, PortalPair> PORTAL_MAP = new HashMap<>();
    private final Map<UUID, PartialPortalPair> PARTIAL_MAP = new HashMap<>();

    private ClientPortalManager() { }

    public static ClientPortalManager getInstance() {
        if(instance == null)
            instance = new ClientPortalManager();
        return instance;
    }

    public void clear() {
        PORTAL_MAP.clear();
        PARTIAL_MAP.clear();
    }

    public void put(UUID gunUUID, PortalEnd end, PortalEntity portal) {
        PortalPair pair = PORTAL_MAP.getOrDefault(gunUUID, new PortalPair());
        pair.set(end, portal);
        PORTAL_MAP.put(gunUUID, pair);
    }

    public void remove(UUID gunUUID, PortalEntity portal) {
        PORTAL_MAP.computeIfPresent(gunUUID, (uuid, pair) -> {
            pair.remove(portal);
            return !pair.isEmpty() ? pair : null;
        });
    }

    public boolean has(UUID gunUUID, PortalEnd end) {
        return PORTAL_MAP.containsKey(gunUUID) && PORTAL_MAP.get(gunUUID).has(end);
    }

    public boolean hasPartial(UUID gunUUID, PortalEnd end) {
        return PARTIAL_MAP.containsKey(gunUUID) && PARTIAL_MAP.get(gunUUID).has(end);
    }

    public boolean hasFullOrPartial(UUID gunUUID, PortalEnd end) {
        return this.has(gunUUID, end) || this.hasPartial(gunUUID, end);
    }

    @Nullable
    public PortalEntity get(UUID gunUUID, PortalEnd end) {
        return PORTAL_MAP.containsKey(gunUUID) ? PORTAL_MAP.get(gunUUID).get(end) : null;
    }

    @Nullable
    public PartialPortal getPartial(UUID gunUUID, PortalEnd end) {
        return PARTIAL_MAP.containsKey(gunUUID) ? PARTIAL_MAP.get(gunUUID).get(end) : null;
    }

    public void forgetPortal(UUID gunUUID, PortalEnd end) {
        if(PORTAL_MAP.containsKey(gunUUID))
            PORTAL_MAP.get(gunUUID).set(end, null);
        if(PARTIAL_MAP.containsKey(gunUUID))
            PARTIAL_MAP.get(gunUUID).set(end, null);
    }

    public Map<UUID, PortalPair> getPortalMap() {
        return PORTAL_MAP;
    }

    public Map<UUID, PartialPortalPair> getPartialMap() {
        return PARTIAL_MAP;
    }
}