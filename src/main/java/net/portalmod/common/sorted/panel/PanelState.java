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
    TOP_RIGHT("top_right"),
    FLOOR_BOTTOM_LEFT("floor_bottom_left"),
    FLOOR_BOTTOM_RIGHT("floor_bottom_right"),
    FLOOR_TOP_LEFT("floor_top_left"),
    FLOOR_TOP_RIGHT("floor_top_right");

    public final String name;

    PanelState(String name) {
        this.name = name;
    }

    public static PanelState doubleState(boolean bottom) {
        return bottom ? BOTTOM : TOP;
    }

    public static PanelState wallState(boolean bottom, boolean left) {
        return bottom ? left ? BOTTOM_LEFT : BOTTOM_RIGHT : left ? TOP_LEFT : TOP_RIGHT;
    }

    public static PanelState floorState(boolean bottom, boolean left) {
        return bottom ? left ? FLOOR_BOTTOM_LEFT : FLOOR_BOTTOM_RIGHT : left ? FLOOR_TOP_LEFT : FLOOR_TOP_RIGHT;
    }

    public boolean isSingle() {
        return this == SINGLE;
    }

    public boolean isDouble() {
        return this == BOTTOM || this == TOP;
    }

    public boolean isWall() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT || this == TOP_LEFT || this == TOP_RIGHT;
    }

    public boolean isFloor() {
        return this == FLOOR_BOTTOM_LEFT || this == FLOOR_BOTTOM_RIGHT || this == FLOOR_TOP_LEFT || this == FLOOR_TOP_RIGHT;
    }

    public boolean isLeft() {
        return this == BOTTOM_LEFT || this == TOP_LEFT || this == FLOOR_BOTTOM_LEFT || this == FLOOR_TOP_LEFT;
    }

    public boolean isBottom() {
        return this == BOTTOM || this == BOTTOM_LEFT || this == BOTTOM_RIGHT || this == FLOOR_BOTTOM_LEFT || this == FLOOR_BOTTOM_RIGHT;
    }

    public PanelState mirrorUpDown() {
        switch (this) {
            case TOP:
                return BOTTOM;
            case TOP_LEFT:
                return BOTTOM_LEFT;
            case TOP_RIGHT:
                return BOTTOM_RIGHT;
            case FLOOR_TOP_LEFT:
                return FLOOR_BOTTOM_LEFT;
            case FLOOR_TOP_RIGHT:
                return FLOOR_BOTTOM_RIGHT;
            case BOTTOM:
                return TOP;
            case BOTTOM_LEFT:
                return TOP_LEFT;
            case BOTTOM_RIGHT:
                return TOP_RIGHT;
            case FLOOR_BOTTOM_LEFT:
                return FLOOR_TOP_LEFT;
            case FLOOR_BOTTOM_RIGHT:
                return FLOOR_TOP_RIGHT;
        }
        throw new IllegalStateException(this.name + " state has no vertical direction");
    }

    public PanelState mirrorLeftRight() {
        switch (this) {
            case TOP_LEFT:
                return TOP_RIGHT;
            case TOP_RIGHT:
                return TOP_LEFT;
            case BOTTOM_LEFT:
                return BOTTOM_RIGHT;
            case BOTTOM_RIGHT:
                return BOTTOM_LEFT;
            case FLOOR_TOP_LEFT:
                return FLOOR_TOP_RIGHT;
            case FLOOR_TOP_RIGHT:
                return FLOOR_TOP_LEFT;
            case FLOOR_BOTTOM_LEFT:
                return FLOOR_BOTTOM_RIGHT;
            case FLOOR_BOTTOM_RIGHT:
                return FLOOR_BOTTOM_LEFT;
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
