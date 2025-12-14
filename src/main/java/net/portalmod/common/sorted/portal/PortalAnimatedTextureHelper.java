package net.portalmod.common.sorted.portal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;

public class PortalAnimatedTextureHelper {
    private static final HashMap<ResourceLocation, Optional<Dimension>> TEXTURE_SIZES = new HashMap<>();

    public static Optional<Dimension> getTextureSize(ResourceLocation location) {
        IResourceManager rm = Minecraft.getInstance().getResourceManager();

        if(!TEXTURE_SIZES.containsKey(location)) {
            try(SimpleTexture.TextureData data = SimpleTexture.TextureData.load(rm, location)) {
                TEXTURE_SIZES.put(location, Optional.of(new Dimension(data.getImage().getWidth(), data.getImage().getHeight())));
            } catch(Exception e) {
                TEXTURE_SIZES.put(location, Optional.empty());
            }
        }

        return TEXTURE_SIZES.get(location);
    }
}