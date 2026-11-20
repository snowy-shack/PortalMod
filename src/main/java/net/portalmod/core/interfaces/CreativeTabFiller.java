package net.portalmod.core.interfaces;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** An item (or a block's item) that adds its own stacks to the creative tab (was fillItemCategory). */
public interface CreativeTabFiller {
    void fillCreativeTab(List<ItemStack> stacks);
}
