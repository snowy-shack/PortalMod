package net.portalmod.common.sorted.portalgun.api;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;

import java.io.IOException;
import java.net.URL;

public class SkinTexture extends Texture {
    private static final String BASE_URL = "https://cdn.portalmod.net/skins/";
    private final String skinId;
    private NativeImage ni;

    public SkinTexture(String skinId) {
        this.skinId = skinId;
    }

    @Override
    public void load(IResourceManager rm) throws IOException {
        this.load();
    }

    public void load() throws IOException {
        if(ni != null)
            return;

        URL url = new URL(BASE_URL + skinId + ".png");
        ni = NativeImage.read(url.openStream());
        if(!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.upload(ni));
        } else {
            this.upload(ni);
        }
    }

    private void upload(NativeImage ni) {
        TextureUtil.prepareImage(this.getId(), 0, ni.getWidth(), ni.getHeight());
        ni.upload(0, 0, 0, 0, 0,
                ni.getWidth(), ni.getHeight(), false, false, false, true);
    }

    public int getFrameCount() {
        return ni == null ? 0 : (ni.getHeight() / 16);
    }
}