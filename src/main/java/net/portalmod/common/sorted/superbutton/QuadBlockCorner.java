package net.portalmod.common.sorted.superbutton;

import net.minecraft.util.IStringSerializable;

public enum QuadBlockCorner implements IStringSerializable {
    UP_LEFT("up_left", 0, 1, 90),
    UP_RIGHT("up_right", 1, 1, 0),
    DOWN_LEFT("down_left", 0, 0, 180),
    DOWN_RIGHT("down_right", 1, 0, -90);
    
    private final String name;
    private final int x, y;
    private final int rot;
    
    QuadBlockCorner(String name, int x, int y, int rot) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.rot = rot;
    }
    
    public String toString() {
       return this.getSerializedName();
    }
    
    public String getSerializedName() {
       return name;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getRot() {
        return this.rot;
    }
    
    public static QuadBlockCorner fromCoords(double dx, double dy) {
        int x = dx > 0 ? 1 : 0;
        int y = dy > 0 ? 1 : 0;
        
        if(x == 0) {
            if(y == 0)
                return DOWN_LEFT;
            return UP_LEFT;
        } else {
            if(y == 0)
                return DOWN_RIGHT;
            return UP_RIGHT;
        }
    }

    public boolean isLeft() {
        return this == UP_LEFT || this == DOWN_LEFT;
    }

    public boolean isUp() {
        return this == UP_LEFT || this == UP_RIGHT;
    }
 }