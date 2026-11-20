package net.portalmod.mixins.hooks.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client side of Forge's WorldEvent.Load. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelHooksMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void pmOnLoad(CallbackInfo ci) {
        ClientEvents.onClientLevelLoad();
    }
}
