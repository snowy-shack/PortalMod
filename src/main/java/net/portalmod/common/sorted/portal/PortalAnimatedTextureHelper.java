package net.portalmod.common.sorted.portal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.HashMap;

public class PortalAnimatedTextureHelper {
    private static final HashMap<ResourceLocation, Dimension> TEXTURE_SIZES = new HashMap<>();

    public static Dimension getTextureSize(ResourceLocation location) {
        IResourceManager rm = Minecraft.getInstance().getResourceManager();

        if(!TEXTURE_SIZES.containsKey(location)) {
            try(SimpleTexture.TextureData data = SimpleTexture.TextureData.load(rm, location)) {
                TEXTURE_SIZES.put(location, new Dimension(data.getImage().getWidth(), data.getImage().getHeight()));
            } catch(Exception e) {
                e.printStackTrace();
                return new Dimension(0, 0);
            }
        }

        return TEXTURE_SIZES.get(location);
    }
}