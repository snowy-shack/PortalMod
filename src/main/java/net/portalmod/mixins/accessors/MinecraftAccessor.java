package net.portalmod.mixins.accessors;

import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.Splashes;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor(value = "splashManager")
    void pmSetSplashManager(Splashes splashManager);

    @Accessor(value = "mainRenderTarget")
    void pmSetMainRenderTarget(Framebuffer framebuffer);

    @Accessor(value = "pausePartialTick")
    float pmGetPausePartialTick();
}