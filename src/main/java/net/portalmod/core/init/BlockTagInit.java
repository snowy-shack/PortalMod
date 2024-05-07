package net.portalmod.core.init;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.portalmod.PortalMod;

public class BlockTagInit {
    private BlockTagInit() {}
    public static void init() {}
    
    public static final Tags.IOptionalNamedTag<Block> PORTALABLE = blockTag("portalable");
    public static final Tags.IOptionalNamedTag<Block> UNPORTALABLE = blockTag("unportalable");
    public static final Tags.IOptionalNamedTag<Block> PORTAL_TRANSPARENT = blockTag("portal_transparent");
    public static final Tags.IOptionalNamedTag<Block> PORTAL_NONBLOCKING = blockTag("portal_nonblocking");
    public static final Tags.IOptionalNamedTag<Block> ANTLINE_CONNECTABLE = blockTag("antline_connectable");

    public static Tags.IOptionalNamedTag<Block> blockTag(String name) {
        return BlockTags.createOptional(new ResourceLocation(PortalMod.MODID, name));
    }

    public static boolean isPortalable(Block block) {
        boolean whitelist = true; // placeholder for config option
        return whitelist ? block.is(PORTALABLE) : !block.is(UNPORTALABLE);
    }
}