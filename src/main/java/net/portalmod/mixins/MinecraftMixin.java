package net.portalmod.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.KeyInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow protected int missTime;
    
    @Inject(method = "continueAttack(Z)V", at = @At(value = "HEAD"), cancellable = true)
    private void pmContinueAttack(CallbackInfo info) {
        if(((Minecraft)(Object)this).player.getMainHandItem().getItem() instanceof PortalGun) {
            this.missTime = 0;
            info.cancel();
        }
    }

    @Redirect(
                        method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
                    ordinal = 1
            )
    )
    private void pmCancelInventoryOpen(Minecraft minecraft, Screen screen) {
        if (minecraft.player != null
                && minecraft.player.getMainHandItem().getItem() instanceof PortalGun
                && KeyInit.PORTALGUN_INTERACT.getKey() == minecraft.options.keyInventory.getKey()) {
            return;
        }

        minecraft.setScreen(screen);
    }
}