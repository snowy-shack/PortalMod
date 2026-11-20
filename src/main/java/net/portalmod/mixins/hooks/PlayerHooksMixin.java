package net.portalmod.mixins.hooks;

import net.minecraft.world.entity.player.Player;
import net.portalmod.core.event.CommonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's PlayerTickEvent (START and END phases). */
@Mixin(Player.class)
public abstract class PlayerHooksMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void pmOnTickStart(CallbackInfo ci) {
        CommonEvents.onPlayerTick((Player) (Object) this, true);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void pmOnTickEnd(CallbackInfo ci) {
        CommonEvents.onPlayerTick((Player) (Object) this, false);
    }
}
