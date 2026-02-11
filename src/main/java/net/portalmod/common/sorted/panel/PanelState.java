package net.portalmod.common.sorted.panel;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public enum PanelState implements IStringSerializable {
    SINGLE("single"),
    BOTTOM("bottom"),
    TOP("top"),
    BOTTOM_LEFT("bottom_left"),
    BOTTOM_RIGHT("bottom_right"),
    TOP_LEFT("top_left"),
    TOP_RIGHT("top_right");

    public final String name;

    PanelState(String name) {
        this.name = name;
    }

    public boolean isSingle() {
        return this == SINGLE;
    }

    public boolean isDouble() {
        return this == BOTTOM || this == TOP;
    }

    public boolean isQuadruple() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT || this == TOP_LEFT || this == TOP_RIGHT;
    }

    public boolean isLeft() {
        return this == BOTTOM_LEFT || this == TOP_LEFT;
    }

    public boolean isBottom() {
        return this == BOTTOM || this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }

    public PanelState oppositeVertical() {
        switch (this) {
            case TOP:
                return BOTTOM;
            case TOP_LEFT:
                return BOTTOM_LEFT;
            case TOP_RIGHT:
                return BOTTOM_RIGHT;
            case BOTTOM:
                return TOP;
            case BOTTOM_LEFT:
                return TOP_LEFT;
            case BOTTOM_RIGHT:
                return TOP_RIGHT;
        }
        throw new IllegalStateException(this.name + " state has no vertical direction");
    }

    public PanelState oppositeHorizontal() {
        switch (this) {
            case TOP_LEFT:
                return TOP_RIGHT;
            case TOP_RIGHT:
                return TOP_LEFT;
            case BOTTOM_LEFT:
                return BOTTOM_RIGHT;
            case BOTTOM_RIGHT:
                return BOTTOM_LEFT;
        }
        throw new IllegalStateException(this.name + " state has no horizontal direction");
    }

    public Direction getVerticalFacing() {
        switch (this) {
            case TOP:
            case TOP_LEFT:
            case TOP_RIGHT:
                return Direction.DOWN;
            case BOTTOM:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                return Direction.UP;
        }
        throw new IllegalStateException(this.name + " state has no vertical direction");
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
