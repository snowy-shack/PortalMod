package net.portalmod.core.init;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

public class ItemTagInit {
    private ItemTagInit() {}
    public static void init() {}

    public static final Tags.IOptionalNamedTag<Item> LUNECAST = itemTag("lunecast");
    public static final Tags.IOptionalNamedTag<Item> BLACKPLATE = itemTag("blackplate");
    public static final Tags.IOptionalNamedTag<Item> GOO_PROTECTION = itemTag("goo_protection");
    public static final Tags.IOptionalNamedTag<Item> CUBES = itemTag("cubes");
    public static final Tags.IOptionalNamedTag<Item> INDICATORS = itemTag("indicators");
    public static final Tags.IOptionalNamedTag<Item> TESTING_ELEMENTS = itemTag("testing_elements");
    public static final Tags.IOptionalNamedTag<Item> IRON_FRAMES = itemTag("iron_frames");
    public static final Tags.IOptionalNamedTag<Item> CHAMBER_DECORATION = itemTag("chamber_decoration");
    public static final Tags.IOptionalNamedTag<Item> PRESSURE_PLATES = itemTag("pressure_plates");

    public static Tags.IOptionalNamedTag<Item> itemTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(PortalMod.MODID, name));
    }

}