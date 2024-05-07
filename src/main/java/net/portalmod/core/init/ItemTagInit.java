package net.portalmod.core.init;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

public class ItemTagInit {
    private ItemTagInit() {}
    public static void init() {}

    public static final Tags.IOptionalNamedTag<Item> GOO_PROTECTION = itemTag("goo_protection");

    public static Tags.IOptionalNamedTag<Item> itemTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(PortalMod.MODID, name));
    }

}