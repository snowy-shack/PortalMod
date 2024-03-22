package net.portalmod.common.sorted.portalgun.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDeserializer extends ArrayList<PlayerDeserializer.Player> {
    private static final long serialVersionUID = 1L;
    
    static class Player {
        UUID uuid;
        List<UUID> skins;
        boolean all_skins;
        boolean custom_colour;
    }
}