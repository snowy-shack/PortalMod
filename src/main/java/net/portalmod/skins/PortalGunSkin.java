package net.portalmod.skins;

import java.util.ArrayList;

public class PortalGunSkin {
    String skin_id;
    String name;
    String description;
    int framerate;
    boolean tintable;
    String checksum;
    String artist;

    protected static final class Deserializer extends ArrayList<PortalGunSkin> {}
}