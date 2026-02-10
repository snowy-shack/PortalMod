package net.portalmod.core.util;

import net.minecraft.util.math.MathHelper;
import net.portalmod.core.math.Vec3;

public class Colour {

    public static final Colour WHITE = new Colour(1f, 1f, 1f, 1f);

    private int r;
    private int g;
    private int b;
    private int a;
    
    public Colour(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    public Colour(float r, float g, float b, float a) {
        this((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
    }

    public Colour(float[] rgb) {
        this(rgb[0], rgb[1], rgb[2], 1);
    }
    
    public Colour(int argb) {
        this.a = (argb >> 24) & 0xFF;
        this.r = (argb >> 16) & 0xFF;
        this.g = (argb >> 8)  & 0xFF;
        this.b = argb & 0xFF;
    }

    public void lighten(float amount) {
        this.r = (int) MathHelper.clamp(this.r + amount * 255, 0, 255);
        this.g = (int) MathHelper.clamp(this.g + amount * 255, 0, 255);
        this.b = (int) MathHelper.clamp(this.b + amount * 255, 0, 255);
    }

    public Colour opaque() {
        this.a = 255;
        return this;
    }

    public static Colour fromHSV(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60 % 2) - 1));
        float m = v - c;

        float rr = 0;
        float gg = 0;
        float bb = 0;

        if(h < 60) {
            rr = c;
            gg = x;
        } else if(h < 120) {
            rr = x;
            gg = c;
        } else if(h < 180) {
            gg = c;
            bb = x;
        } else if(h < 240) {
            gg = x;
            bb = c;
        } else if(h < 300) {
            bb = c;
            rr = x;
        } else {
            bb = x;
            rr = c;
        }

        return new Colour(rr + m, gg + m, bb + m, 1f);
    }

    public Vec3 getHSV() {
        float rr = r / 255f;
        float gg = g / 255f;
        float bb = b / 255f;

        float max = Math.max(rr, Math.max(gg, bb));
        float min = Math.min(rr, Math.min(gg, bb));
        float delta = max - min;

        float h = 0;
        if(delta == 0) {
            h = 0;
        } else if(max == rr) {
            h = 60 * ((gg - bb) / delta % 6);
        } else if(max == gg) {
            h = 60 * ((bb - rr) / delta + 2);
        } else if(max == bb) {
            h = 60 * ((rr - gg) / delta + 4);
        }

        return new Vec3(h, (max == 0) ? 0 : (delta / max), max);
    }
    
    public int getValue() {
        return (a & 0xFF << 24) | (r & 0xFF << 16) | (g & 0xFF << 8) | (b & 0xFF);
    }

    public int getRGBValue() {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
    
    public int getIntR() { return r; }
    public int getIntG() { return g; }
    public int getIntB() { return b; }
    public int getIntA() { return a; }
    
    public float getFloatR() { return r / 255f; }
    public float getFloatG() { return g / 255f; }
    public float getFloatB() { return b / 255f; }
    public float getFloatA() { return a / 255f; }
}