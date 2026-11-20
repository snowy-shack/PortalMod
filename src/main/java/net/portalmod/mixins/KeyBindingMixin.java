package net.portalmod.mixins;

import net.portalmod.core.init.KeyInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public abstract class KeyBindingMixin {
    @Shadow public abstract boolean isDefault();

    @Inject(method = "same", at = @At("HEAD"), cancellable = true)
    public void pmMakeEKeyNotConflict(KeyMapping keyBinding, CallbackInfoReturnable<Boolean> cir) {
        KeyMapping self = (KeyMapping)(Object)this;
        if (self == KeyInit.PORTALGUN_INTERACT && self.isDefault() || keyBinding == KeyInit.PORTALGUN_INTERACT && keyBinding.isDefault()) {
            cir.setReturnValue(false);
        }
    }
}
