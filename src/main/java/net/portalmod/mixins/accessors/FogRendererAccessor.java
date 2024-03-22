package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.FogRenderer;

@Mixin(FogRenderer.class)
public interface FogRendererAccessor {
    @Accessor(remap = false, value = "fogRed")
    static float pmGetFogRed() {
        throw new AssertionError();
    }
    
    @Accessor(remap = false, value = "fogGreen")
    static float pmGetFogGreen() {
        throw new AssertionError();
    }
    
    @Accessor(remap = false, value = "fogBlue")
    static float pmGetFogBlue() {
        throw new AssertionError();
    }
}