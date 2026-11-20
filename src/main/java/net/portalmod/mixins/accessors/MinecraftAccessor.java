package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SplashManager;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor(value = "splashManager")
    void pmSetSplashManager(SplashManager splashManager);

    @Accessor(value = "mainRenderTarget")
    void pmSetMainRenderTarget(RenderTarget framebuffer);

    @Accessor(value = "pausePartialTick")
    float pmGetPausePartialTick();
}