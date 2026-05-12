package net.portalmod.common.sorted.portalgun.skins;

import java.util.ArrayList;

public class PortalGunSkin {
    public String skin_id;
    public String name;
    public String description;
    public int framerate;
    public boolean tintable;
    public String checksum;
    public String checksum_anim;
    public String artist;

    public boolean isAnimated() {
        return this.framerate > 0;
    }

    public String getFilename() {
        return this.skin_id + (this.isAnimated() ? "_anim" : "");
    }

    public String getChecksum() {
        return this.isAnimated() ? this.checksum_anim : this.checksum;
    }

    protected static final class Deserializer extends ArrayList<PortalGunSkin> {}
}