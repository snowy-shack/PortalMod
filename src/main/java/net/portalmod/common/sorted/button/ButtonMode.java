package net.portalmod.common.sorted.button;


import net.minecraft.util.StringRepresentable;

public enum ButtonMode implements StringRepresentable {
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