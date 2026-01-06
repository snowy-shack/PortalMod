package net.portalmod.client.animation;

import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;

public class AnimatedTexture {
    private final RenderMaterial material;
    private final int frames;
    private final int framerate;
    
    public AnimatedTexture(ResourceLocation atlas, ResourceLocation texture, int frames, int framerate) {
        this.material = new RenderMaterial(atlas, texture);
        this.frames = frames;
        this.framerate = framerate;
    }

    public ResourceLocation getTextureLocation() {
        return this.material.texture();
    }
    
    public IVertexBuilder buffer(IRenderTypeBuffer renderTypeBuffer, Function<ResourceLocation, RenderType> renderType) {
        return material.buffer(renderTypeBuffer, renderType);
    }
    
    public void setupAnimation() {
        float height = material.sprite().getV1();
        float index = framerate == 0  || frames == 0
                ? 0 : (System.currentTimeMillis() / (1000 / framerate) % frames);
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0, height / frames * index, 0);
        RenderSystem.scalef(1, 1f / frames, 1);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }
    
    public void endAnimation() {
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }
}