package net.portalmod.mixins.hooks.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's RenderPlayerEvent.Pre (used to change arm poses). */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererHooksMixin {
    @Inject(method = "setModelProperties", at = @At("TAIL"))
    private void pmAfterModelProperties(AbstractClientPlayer player, CallbackInfo ci) {
        ClientEvents.onPlayerRender(player, (PlayerRenderer) (Object) this);
    }
}
