package net.portalmod.common.sorted.portalgun.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.client.animation.AnimatedTexture;

public class SkinLoader {
    public static AnimatedTexture loadSkin(String skinId) {
        AnimatedTexture animatedTexture;

        try {
            SkinTexture skinTexture = new SkinTexture(skinId);
            TextureManager tm = Minecraft.getInstance().textureManager;
            tm.register(new ResourceLocation(PortalMod.MODID, "gun/" + skinId), skinTexture);

            animatedTexture = new AnimatedTexture(AtlasTexture.LOCATION_BLOCKS,
                    new ResourceLocation(PortalMod.MODID, "gun/" + skinId), skinTexture.getFrameCount(), 2);

            return animatedTexture;
        } catch(Exception e) {
        }

        return null;
    }
}