package net.portalmod.mixins.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.portalmod.common.sorted.portal.PortalRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Framebuffer.class)
public class FramebufferMixin {

    // BEWARE: PORTAL RENDERING
    @ModifyVariable(
            remap = false,
            method = "bindWrite",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private boolean pmBindMainFBWithViewport(boolean value) {
        return value || ((Object)this == Minecraft.getInstance().getMainRenderTarget());
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "clear",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmClear(boolean resize, CallbackInfo info) {
        if(Minecraft.getInstance().level != null && !PortalRenderer.getInstance().canClear)
            info.cancel();
    }
}