package net.portalmod.mixins.screen;

import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.item.ItemStack;
import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeScreen.class)
public abstract class CreativeScreenMixin {

    @Redirect(method = "slotClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", ordinal = 1))
    public ItemStack pmResetGunUUIDOnCopy(ItemStack instance) {
        ItemStack itemStack = instance.copy();
        if (itemStack.getItem() instanceof PortalGun) {
            PortalGun.onDuplicate(itemStack);
        }
        return itemStack;
    }
}
