package net.portalmod.client.render;

import com.mojang.blaze3d.matrix.*;
import com.mojang.blaze3d.systems.*;

import net.minecraft.client.*;
import net.minecraft.client.renderer.*;

public class PortalModRenderer {
    public static void render(float elapsedTicks, MatrixStack stack) {
        Minecraft.getInstance().getMainRenderTarget().unbindWrite();

        RenderSystem.pushMatrix();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        
        FogRenderer.setupNoFog();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        
        if(!Minecraft.getInstance().noRender)
//            PortalGameRenderer.renderLevel(elapsedTicks, Util.getNanos(), stack);

//        frameBuffer.unbindWrite();
        RenderSystem.popMatrix();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
}