package net.portalmod.common.sorted.portal;

import java.awt.*;

public enum PortalColors {
    black      (0x37363a),
    blue       (0x0593f7),
    brown      (0x945a28),
    cyan       (0x2bbdab),
    gray       (0x37363a),
    green      (0x506e1a),
    light_blue (0x32c2ef),
    light_gray (0x9d9d97),
    lime       (0x80c71f),
    magenta    (0xc048b6),
    orange     (0xfc962d),
    pink       (0xf86b9d),
    purple     (0x8636c7),
    red        (0xd24e29),
    white      (0xe0dfd8),
    yellow     (0xf1c734);

    private final Color color;

    PortalColors(int hex) {
        this.color = new Color(hex);
    }

    public static Color getColor(String color) {
        return valueOf(color.toLowerCase()).color;
    }

    public static Color getColor(PortalEntity portal) {
        return getColor(portal.getColor());
    }
}