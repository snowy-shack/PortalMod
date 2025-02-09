package net.portalmod.mixins.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.portalmod.common.sorted.gel.container.GelContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
    public BlockItemMixin(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V"), remap = false)
    public void shrink(ItemStack instance, int i) {
        Item item = instance.getItem();
        if (item instanceof GelContainer) {
            GelContainer.decreaseAmount(instance);
        } else {
            instance.shrink(i);
        }
    }
}
