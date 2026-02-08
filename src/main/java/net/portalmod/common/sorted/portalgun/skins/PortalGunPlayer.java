package net.portalmod.common.sorted.portalgun.skins;

import java.util.ArrayList;

public class PortalGunPlayer {
    public int default_color;
    public ArrayList<String> skins;

    public PortalGunPlayer(int defaultColor, ArrayList<String> skins) {
        this.default_color = defaultColor;
        this.skins = skins;
    }

    public PortalGunPlayer(ArrayList<String> skins) {
        this(0, skins);
    }

    public PortalGunPlayer() {
        this(new ArrayList<>());
    }
}