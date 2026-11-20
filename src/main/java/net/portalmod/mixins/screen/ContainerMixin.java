package net.portalmod.mixins.screen;

import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMixin {

    @Redirect(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", ordinal = 4))
    public ItemStack pmResetGunUUIDOnCopy(ItemStack instance) {
        ItemStack itemStack = instance.copy();
        if (itemStack.getItem() instanceof PortalGun) {
            PortalGun.onDuplicate(itemStack);
        }
        return itemStack;
    }
}
