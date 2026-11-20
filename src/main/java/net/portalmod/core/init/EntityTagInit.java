package net.portalmod.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import net.portalmod.PortalMod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.resources.ResourceLocation;

public class EntityTagInit {
    private EntityTagInit() {}
    public static void init() {}
    
    public static final TagKey<EntityType<?>> FIZZLER_NO_ITEM_DROPS = tag("fizzler_no_item_drops");
    public static final TagKey<EntityType<?>> BUTTON_NO_PRESS = tag("super_button_lightweight");

    public static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(PortalMod.MODID, name));
    }
}