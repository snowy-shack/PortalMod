package net.portalmod.mixins.hooks.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's RenderGameOverlayEvent.Post for the crosshair. */
@Mixin(Gui.class)
public abstract class GuiHooksMixin {
    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void pmAfterCrosshair(GuiGraphics graphics, CallbackInfo ci) {
        ClientEvents.onRenderCrosshair(graphics);
    }
}
