package net.portalmod.skins;

import java.util.ArrayList;
import java.util.UUID;

public class PortalGunSkin {
    UUID id;
    String name;
    String description;
    int framerate;
    boolean tintable;
    int priority;
    int owners;
    String checksum;

    protected static final class Deserializer extends ArrayList<PortalGunSkin> {}
}