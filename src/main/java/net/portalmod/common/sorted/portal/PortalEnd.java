package net.portalmod.common.sorted.portal;

import net.minecraft.util.IStringSerializable;

public enum PortalEnd implements IStringSerializable {
    NONE("none"),
    PRIMARY("primary"),
    SECONDARY("secondary");

    private final String name;

    PortalEnd(String name) {
        this.name = name;
    }
    public String toString() {
        return this.getSerializedName();
    }
    public String getSerializedName() {
        return name;
    }
    public PortalEnd other() {
        if(this == PRIMARY)
            return SECONDARY;
        if(this == SECONDARY)
            return PRIMARY;
        return NONE;
    }
}