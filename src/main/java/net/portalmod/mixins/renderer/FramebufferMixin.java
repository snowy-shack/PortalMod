package net.portalmod.mixins.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;

@Mixin(RenderTarget.class)
public class FramebufferMixin {

    // BEWARE: PORTAL RENDERING
    @ModifyVariable(
                        method = "bindWrite",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private boolean pmBindMainFBWithViewport(boolean value) {
        return value || ((Object)this == Minecraft.getInstance().getMainRenderTarget());
    }
}