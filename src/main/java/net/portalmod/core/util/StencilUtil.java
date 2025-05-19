package net.portalmod.core.util;

import static org.lwjgl.opengl.GL11.*;

import com.mojang.blaze3d.systems.RenderSystem;

public class StencilUtil {
    public static final StencilUtil INSTANCE = new StencilUtil();
    
    private StencilUtil() {}
    
    public StencilUtil enable() {
        glEnable(GL_STENCIL_TEST);
        return this;
    }
    
    public StencilUtil disable() {
        glDisable(GL_STENCIL_TEST);
        return this;
    }
    
    public StencilUtil mask(int mask) {
        RenderSystem.stencilMask(mask);
        return this;
    }
    
    public StencilUtil read() {
        this.mask(0);
        return this;
    }
    
    public StencilUtil write() {
        this.mask(0xFFFFFFFF);
        return this;
    }
    
    public StencilUtil read(int func, int ref) {
        this.read();
        this.func(func, ref, 0x7F);
        return this;
    }
    
    public StencilUtil write(int func, int ref, int sfail, int dpfail, int dppass) {
        this.write();
        this.func(func, ref, 0x7F);
        this.op(sfail, dpfail, dppass);
        return this;
    }
    
    public StencilUtil func(int func, int ref, int mask) {
        RenderSystem.stencilFunc(func, ref, mask);
        return this;
    }
    
    public StencilUtil op(int sfail, int dpfail, int dppass) {
        RenderSystem.stencilOp(sfail, dpfail, dppass);
        return this;
    }
    
    public StencilUtil clear() {
        this.write();
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL_STENCIL_BUFFER_BIT, false);
        return this;
    }
}