package net.portalmod.mixins.entity;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(
            remap = false,
            method = "drop",
            at = @At("HEAD")
    )
    private void pmStopHoldingSoundOnDrop(boolean all, CallbackInfoReturnable<Boolean> info) {
        ClientPlayerEntity thiss = (ClientPlayerEntity)(Object)this;
        ItemStack selected = thiss.inventory.getSelected();

        if(selected.getItem() instanceof PortalGun) {
            PortalGun.dropCube(thiss, selected);
        }
    }
}