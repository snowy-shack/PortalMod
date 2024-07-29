package net.portalmod.core.init;

import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

public class EntityTagInit {
    private EntityTagInit() {}
    public static void init() {}
    
    public static final Tags.IOptionalNamedTag<EntityType<?>> FIZZLER_NO_ITEM_DROPS = blockTag("fizzler_no_item_drops");

    public static Tags.IOptionalNamedTag<EntityType<?>> blockTag(String name) {
        return EntityTypeTags.createOptional(new ResourceLocation(PortalMod.MODID, name));
    }
}