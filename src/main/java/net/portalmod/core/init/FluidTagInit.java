package net.portalmod.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import net.portalmod.PortalMod;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.resources.ResourceLocation;

public class FluidTagInit {
    private FluidTagInit() {}
    public static void init() {}
    
    public static final TagKey<Fluid> GOO = TagKey.create(Registries.FLUID, 
            new ResourceLocation(PortalMod.MODID, "goo"));
}