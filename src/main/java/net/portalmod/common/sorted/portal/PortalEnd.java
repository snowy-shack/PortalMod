package net.portalmod.common.sorted.portal;

import net.minecraft.util.StringRepresentable;

public enum PortalEnd implements StringRepresentable {
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

    public enum Safe {
        PRIMARY(PortalEnd.PRIMARY),
        SECONDARY(PortalEnd.SECONDARY);

        private final PortalEnd original;

        Safe(PortalEnd original) {
            this.original = original;
        }

        public PortalEnd getOriginal() {
            return this.original;
        }
    }
}