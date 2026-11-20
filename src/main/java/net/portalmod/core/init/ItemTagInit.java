package net.portalmod.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import net.portalmod.PortalMod;

import net.minecraft.world.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

public class ItemTagInit {
    private ItemTagInit() {}
    public static void init() {}

    public static final TagKey<Item> LUNECAST = itemTag("lunecast");
    public static final TagKey<Item> BLACKPLATE = itemTag("blackplate");
    public static final TagKey<Item> CUBES = itemTag("cubes");
    public static final TagKey<Item> INDICATORS = itemTag("indicators");
    public static final TagKey<Item> TESTING_ELEMENTS = itemTag("testing_elements");
    public static final TagKey<Item> IRON_FRAMES = itemTag("iron_frames");
    public static final TagKey<Item> CHAMBER_DECORATION = itemTag("chamber_decoration");
    public static final TagKey<Item> PRESSURE_PLATES = itemTag("pressure_plates");

    public static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(PortalMod.MODID, name));
    }

}