package net.portalmod.skins;

import java.util.ArrayList;
import java.util.UUID;

public class PortalGunSkin {
    public String skin_id;
    public String name;
    public String description;
    public int framerate;
    public boolean tintable;
    public String checksum;
    public String artist;

    boolean unlocked;

    public PortalGunSkin(String skin_id, String name, String description, int framerate, boolean tintable,
                         String checksum, String artist, boolean unlocked) {
        this.skin_id = skin_id;
        this.name = name;
        this.description = description;
        this.framerate = framerate;
        this.tintable = tintable;
        this.checksum = checksum;
        this.artist = artist;
        this.unlocked = unlocked;
    }

    protected static final class Deserializer extends ArrayList<PortalGunSkin> {}
}