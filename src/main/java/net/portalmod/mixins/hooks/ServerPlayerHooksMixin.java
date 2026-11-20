package net.portalmod.mixins.hooks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Forge's Item#onDroppedByPlayer, used by the portal gun (drops a held cube first). */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerHooksMixin {
    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    private void pmOnDrop(boolean dropStack, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ItemStack selected = self.getInventory().getSelected();
        if (selected.getItem() instanceof PortalGun gun && !gun.onDroppedByPlayer(selected, self))
            cir.setReturnValue(false);
    }
}
