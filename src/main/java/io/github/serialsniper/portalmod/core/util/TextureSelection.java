package io.github.serialsniper.portalmod.core.util;

import java.awt.*;

public class TextureSelection {
    public int x, y, width, height;

    public TextureSelection(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static class Double {
        private Point inactive, active;
        public int width, height;

        public Double(int x, int y, int width, int height, int offsetX, int offsetY) {
            this.inactive = new Point(x, y);
            this.active = new Point(x + offsetX, y + offsetY);
            this.width = width;
            this.height = height;
        }

        public Point get(boolean active) {
            return active ? this.active : this.inactive;
        }
    }
}