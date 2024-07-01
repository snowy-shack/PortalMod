package net.portalmod.core.util;

import net.minecraft.util.math.MathHelper;

public class Colour {
    private final int r;
    private final int g;
    private final int b;
    private final int a;
    
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
    
    public static Colour fromHSV(int h, float s, float v) {
        float r, g, b;
        
        MathHelper.clamp(s, 0, 1);
        MathHelper.clamp(v, 0, 1);
        
//        if(s <= 0.0)
//            return new Colour(v, v, v, 1);
        
        float hh = h % 360 / 60f;
        long i = (long)hh;
        float ff = hh - i;
        float p = v * (1f - s);
        float q = v * (1f - (s * ff));
        float t = v * (1f - (s * (1f - ff)));
        
        switch((int)i) {
        case 0:  r = v; g = t; b = p; break;
        case 1:  r = q; g = v; b = p; break;
        case 2:  r = p; g = v; b = t; break;
        case 3:  r = p; g = q; b = v; break;
        case 4:  r = t; g = p; b = v; break;
        default: r = v; g = p; b = q; break;
        }
        
        return new Colour(r, g, b, 1);
    }
    
    public int getValue() {
        return (a & 0xFF << 24) | (r & 0xFF << 16) | (g & 0xFF << 8) | (b & 0xFF);
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