package net.portalmod.mixins.hooks;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.portalmod.core.event.CommonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's PlayerEvent.ItemPickupEvent. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityHooksMixin {
    // The stack is emptied while being added to the inventory, so keep a copy from before.
    @Unique
    private ItemStack pmStackBeforePickup = ItemStack.EMPTY;

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void pmBeforeTouch(Player player, CallbackInfo ci) {
        pmStackBeforePickup = ((ItemEntity) (Object) this).getItem().copy();
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private void pmOnPickup(Player player, CallbackInfo ci) {
        CommonEvents.onPlayerPickUpItem(player, (ItemEntity) (Object) this, pmStackBeforePickup);
    }
}
