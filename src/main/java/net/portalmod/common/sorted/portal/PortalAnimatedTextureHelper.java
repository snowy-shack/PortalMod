package net.portalmod.common.sorted.portal;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

public class PortalAnimatedTextureHelper {
    private static final HashMap<ResourceLocation, Optional<Dimension>> TEXTURE_SIZES = new HashMap<>();

    public static Optional<Dimension> getTextureSize(ResourceLocation location) {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        if(!TEXTURE_SIZES.containsKey(location)) {
            try(SimpleTexture.TextureImage data = SimpleTexture.TextureImage.load(rm, location)) {
                TEXTURE_SIZES.put(location, Optional.of(new Dimension(data.getImage().getWidth(), data.getImage().getHeight())));
            } catch(Exception e) {
                TEXTURE_SIZES.put(location, Optional.empty());
            }
        }

        return TEXTURE_SIZES.get(location);
    }
}