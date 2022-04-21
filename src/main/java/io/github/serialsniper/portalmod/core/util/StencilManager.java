//package io.github.serialsniper.portalmod.core.util;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//
//import static org.lwjgl.opengl.GL11.*;
//
//public class StencilManager {
//    private static final StencilManager INSTANCE = new StencilManager();
//
//    private StencilManager() {}
//
//    public static StencilManager get() {
//        return INSTANCE;
//    }
//
//    public StencilManager enable() {
//        glEnable(GL_STENCIL_TEST);
//        return this;
//    }
//
//    public StencilManager write() {
//        RenderSystem.stencilMask(0xFF);
//        return this;
//    }
//
//    public StencilManager read() {
//        RenderSystem.stencilMask(0x00);
//        return this;
//    }
//
//    public void disable() {
//        glDisable(GL_STENCIL_TEST);
//    }
//}