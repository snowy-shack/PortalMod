package net.portalmod.core.init;

import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.resources.ResourceLocation;

public class FluidTagInit {
    private FluidTagInit() {}
    public static void init() {}
    
    public static final Tags.IOptionalNamedTag<Fluid> GOO = FluidTags.createOptional(
            new ResourceLocation(PortalMod.MODID, "goo"));
}