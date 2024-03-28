package net.portalmod.core.init;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

public class FluidTagInit {
    private FluidTagInit() {}
    public static void init() {}
    
    public static final Tags.IOptionalNamedTag<Fluid> GOO = FluidTags.createOptional(
            new ResourceLocation(PortalMod.MODID, "goo"));
}