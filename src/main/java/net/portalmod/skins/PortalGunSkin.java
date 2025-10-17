package net.portalmod.skins;

import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;

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

    public CompoundNBT toNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("SkinId", skin_id);
        tag.putString("Name", name);
        tag.putString("Description", description);
        tag.putInt("Framerate", framerate);
        tag.putBoolean("Tintable", tintable);
        tag.putString("Checksum", checksum);
        tag.putString("Artist", artist);
        return tag;
    }

    public static PortalGunSkin fromNBT(CompoundNBT tag) {
        return new PortalGunSkin(
            tag.getString("SkinId"),
            tag.getString("Name"),
            tag.getString("Description"),
            tag.getInt("Framerate"),
            tag.getBoolean("Tintable"),
            tag.getString("Checksum"),
            tag.getString("Artist"),
            false
        );
    }

    protected static final class Deserializer extends ArrayList<PortalGunSkin> {}
}