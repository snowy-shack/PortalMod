package net.portalmod.mixins;

import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.portalmod.core.init.ItemInit;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow(remap = false) protected int missTime;
    
    @Inject(remap = false, method = "continueAttack(Z)V", at = @At(value = "HEAD"), cancellable = true)
    private void pmContinueAttack(CallbackInfo info) {
        if(((Minecraft)(Object)this).player.getMainHandItem().getItem() instanceof PortalGun) {
            this.missTime = 0;
            info.cancel();
        }
    }
}