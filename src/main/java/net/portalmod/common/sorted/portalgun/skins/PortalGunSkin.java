package net.portalmod.common.sorted.portalgun.skins;

import java.util.ArrayList;

public class PortalGunSkin {
    public String skin_id;
    public String name;
    public String description;
    public int framerate;
    public boolean tintable;
    public String checksum;
    public String artist;

    protected static final class Deserializer extends ArrayList<PortalGunSkin> {}
}