package net.portalmod.mixins.hooks;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.portalmod.core.event.CommonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces Forge's LivingUpdateEvent and LivingFallEvent. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHooksMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void pmOnTick(CallbackInfo ci) {
        CommonEvents.onLivingUpdate((LivingEntity) (Object) this);
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void pmOnFall(float distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (CommonEvents.shouldCancelFall((LivingEntity) (Object) this))
            cir.setReturnValue(false);
    }
}
