package net.portalmod.common.sorted.portal;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PortalColors {
    private static PortalColors instance;
    private final Map<String, Color> COLORS;

    private PortalColors() {
        this.COLORS = new HashMap<>();
        this.init();
    }

    public static PortalColors getInstance() {
        if(instance == null)
            instance = new PortalColors();
        return instance;
    }

    private void init() {
        this.COLORS.put("black",      new Color(0x37363a));
        this.COLORS.put("blue",       new Color(0x0593f7));
        this.COLORS.put("brown",      new Color(0x945a28));
        this.COLORS.put("cyan",       new Color(0x2bbdab));
        this.COLORS.put("gray",       new Color(0x37363a));
        this.COLORS.put("green",      new Color(0x506e1a));
        this.COLORS.put("light_blue", new Color(0x32c2ef));
        this.COLORS.put("light_gray", new Color(0x9d9d97));

        this.COLORS.put("lime",       new Color(0x80c71f));
        this.COLORS.put("magenta",    new Color(0xc048b6));
        this.COLORS.put("orange",     new Color(0xfc962d));
        this.COLORS.put("pink",       new Color(0xf86b9d));
        this.COLORS.put("purple",     new Color(0x8636c7));
        this.COLORS.put("red",        new Color(0xd24e29));
        this.COLORS.put("white",      new Color(0xe0dfd8));
        this.COLORS.put("yellow",     new Color(0xf1c734));
    }

    public Color getColor(PortalEntity portal) {
        return this.COLORS.get(portal.getColor());
    }
}