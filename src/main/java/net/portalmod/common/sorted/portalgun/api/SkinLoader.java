package net.portalmod.common.sorted.portalgun.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.client.animation.AnimatedTexture;
import net.portalmod.common.sorted.portalgun.PortalGunISTER;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkinLoader {
    private static final Map<String, AnimatedTexture> CACHE = new ConcurrentHashMap<>();

    public static AnimatedTexture loadSkin(String skinId) {
        AnimatedTexture cached = CACHE.get(skinId);
        if (cached != null) return cached;

        try {
            SkinTexture skinTexture = new SkinTexture(skinId);
            TextureManager tm = Minecraft.getInstance().textureManager;
//            ResourceLocation location = new ResourceLocation(PortalMod.MODID, "gun/skin_" + skinId);
            ResourceLocation location = PortalGunISTER.PGUN_TEXTURE_BASE;

            tm.register(location, skinTexture);
            // TODO: Animate
//            AnimatedTexture animatedTexture = new AnimatedTexture(AtlasTexture.LOCATION_BLOCKS, location, 1, 1);
            AnimatedTexture animatedTexture = PortalGunISTER.DEFAULT;

            CACHE.put(skinId, animatedTexture);
            return animatedTexture;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static void invalidate(String skinId) {
        CACHE.remove(skinId);
    }
}