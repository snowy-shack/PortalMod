package net.portalmod.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import net.portalmod.PortalMod;

import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;

public class BlockTagInit {
    private BlockTagInit() {}
    public static void init() {}
    
    public static final TagKey<Block> PORTALABLE = blockTag("portal/portalable");
    public static final TagKey<Block> UNPORTALABLE = blockTag("portal/unportalable");
    public static final TagKey<Block> PORTAL_TRANSPARENT = blockTag("portal/portal_transparent");
    public static final TagKey<Block> PORTAL_NONBLOCKING = blockTag("portal/portal_nonblocking");
    public static final TagKey<Block> PORTAL_INHERITING = blockTag("portal/portal_inheriting");
    public static final TagKey<Block> PORTALABLE_QUALITY = blockTag("portal/portalable_quality");
    public static final TagKey<Block> BLOCK_TRANSPARENT = blockTag("block_transparent");
    public static final TagKey<Block> BLOCK_PERMEABLE = blockTag("block_permeable");

    public static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(PortalMod.MODID, name));
    }
}