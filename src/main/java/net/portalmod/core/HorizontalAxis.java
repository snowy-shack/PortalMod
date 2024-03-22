package net.portalmod.core;

import net.minecraft.util.IStringSerializable;

public enum HorizontalAxis implements IStringSerializable {
    X("x"),
    Z("z");

    private final String name;

    HorizontalAxis(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}