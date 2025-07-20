package net.portalmod.mixins.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
}