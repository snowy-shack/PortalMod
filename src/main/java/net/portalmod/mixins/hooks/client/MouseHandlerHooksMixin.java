package net.portalmod.mixins.hooks.client;

import net.minecraft.client.MouseHandler;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's InputEvent.RawMouseEvent (cancellable). */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerHooksMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void pmOnPress(long window, int button, int action, int mods, CallbackInfo ci) {
        if (ClientEvents.onMouseClick(button, action))
            ci.cancel();
    }
}
