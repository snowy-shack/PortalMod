package net.portalmod.core.init;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

public class BlockTagInit {
    private BlockTagInit() {}
    public static void init() {}
    
    public static final Tags.IOptionalNamedTag<Block> PORTALABLE = BlockTags.createOptional(
            new ResourceLocation(PortalMod.MODID, "portalable"));

    public static final Tags.IOptionalNamedTag<Block> PORTAL_TRANSPARENT = BlockTags.createOptional(
            new ResourceLocation(PortalMod.MODID, "portal_transparent"));

    public static final Tags.IOptionalNamedTag<Block> PORTAL_NONBLOCKING = BlockTags.createOptional(
            new ResourceLocation(PortalMod.MODID, "portal_nonblocking"));
}