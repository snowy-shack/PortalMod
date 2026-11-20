package net.portalmod.mixins.hooks.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.portalmod.core.event.ClientEvents;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's EntityViewRenderEvent.FogDensity and FogColors. */
@Mixin(FogRenderer.class)
public abstract class FogRendererHooksMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void pmOnSetupColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance,
                                       float darkenAmount, CallbackInfo ci) {
        Vector3f goo = ClientEvents.fogColor(camera);
        if (goo != null) {
            fogRed = goo.x();
            fogGreen = goo.y();
            fogBlue = goo.z();
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        }
        ClientEvents.onFogSetup(fogRed, fogGreen, fogBlue);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void pmOnSetupFog(Camera camera, FogRenderer.FogMode mode, float farPlane, boolean thickFog,
                                     float partialTicks, CallbackInfo ci) {
        Float density = ClientEvents.fogDensity(camera);
        if (density != null && density > 0) {
            // 1.16 used exponential fog (GL_EXP). Since 1.17 fog is linear only, so we end the fog
            // where exp(-density * d) drops to about 5% (d = 3 / density). This is an approximation.
            RenderSystem.setShaderFogStart(0.0F);
            RenderSystem.setShaderFogEnd(3.0F / density);
        }
    }
}
