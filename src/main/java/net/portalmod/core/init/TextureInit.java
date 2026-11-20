package net.portalmod.core.init;

import net.minecraft.resources.ResourceLocation;
import net.portalmod.PortalMod;

/**
 * Extra block-atlas sprites. Since 1.19.3 atlases are built from JSON
 * (assets/minecraft/atlases/blocks.json); textures under block/ are included automatically.
 */
public class TextureInit {
    private TextureInit() {}

    public static final ResourceLocation FAITHPLATE_TARGET = new ResourceLocation(PortalMod.MODID, "block/faithplate_target");
}
