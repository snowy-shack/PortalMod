package net.portalmod.mixins.item;

import net.portalmod.common.sorted.gel.container.GelContainer;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
    public BlockItemMixin(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    public void shrink(ItemStack instance, int i) {
        Item item = instance.getItem();
        if (item instanceof GelContainer) {
            GelContainer.decreaseAmount(instance);
        } else {
            instance.shrink(i);
        }
    }

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getPitch()F"))
    private float redirectPitch(SoundType instance) {
        if (instance.getPlaceSound() == SoundInit.GEL_PLACE.get()) {
            return ModUtil.randomSlightSoundPitch();
        }

        return instance.getPitch();
    }
}
