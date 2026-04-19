package net.portalmod.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.portalmod.core.init.ItemInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
                        method = "tagMatches",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void pmPortalGunMatches(ItemStack item, ItemStack otherItem, CallbackInfoReturnable<Boolean> info) {
        if(item.getItem() == ItemInit.PORTALGUN.get() && otherItem.getItem() == ItemInit.PORTALGUN.get()) {
            if(item.hasTag() && otherItem.hasTag()) {
                CompoundNBT nbt1 = item.getTag();
                CompoundNBT nbt2 = otherItem.getTag();
                if(nbt1 != null && nbt2 != null)
                    if(nbt1.contains("gunUUID") && nbt2.contains("gunUUID"))
                        if(nbt1.getUUID("gunUUID").equals(nbt2.getUUID("gunUUID")))
                            info.setReturnValue(true);
            }
        }
    }
}