package net.portalmod.common.sorted.trigger;

import net.minecraft.util.IStringSerializable;

public enum TriggerState implements IStringSerializable {
    NULL("null"),
    INACTIVE("inactive"),
    ACTIVE("active");

    private final String name;

    TriggerState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean isActive(boolean active) {
        return active ? this == ACTIVE : this == INACTIVE;
    }

    public static TriggerState fromActive(boolean active) {
        return active ? ACTIVE : INACTIVE;
    }
}
