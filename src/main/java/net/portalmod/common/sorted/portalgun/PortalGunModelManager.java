package net.portalmod.common.sorted.portalgun;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalGunModelManager {
    private static PortalGunModelManager instance;

    private final Map<UUID, PortalGunModel> PORTALGUN_MODELS = new HashMap<>();

    private PortalGunModelManager() {}

    public static PortalGunModelManager getInstance() {
        if(instance == null)
            instance = new PortalGunModelManager();
        return instance;
    }

    public PortalGunModel getModel(UUID gunUUID) {
        PORTALGUN_MODELS.putIfAbsent(gunUUID, new PortalGunModel());
        return PORTALGUN_MODELS.get(gunUUID);
    }
}