package net.portalmod.common.blocks;


import net.minecraft.util.IStringSerializable;

public enum ButtonMode implements IStringSerializable {
    NORMAL,
    PERSISTENT,
    TOGGLE;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public ButtonMode cycle() {
        return ButtonMode.values()[(this.ordinal() + 1) % 3];
    }
}