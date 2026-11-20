package net.portalmod.client.animation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portalgun.skins.ClientSkinManager;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureAtlas;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class PortalGunAnimatedTexture extends AbstractTexture {
    private static final ResourceLocation DEFAULT_SKIN = new ResourceLocation(PortalMod.MODID, "textures/portalgun/default.png");
    private final String skinId;
    private final String skinFilename;
    private final Material material;
    private final int framerate;
    private NativeImage ni;
    
    public PortalGunAnimatedTexture(String id, String skinFilename, int framerate) {
        this.skinId = id;
        this.skinFilename = skinFilename;
        this.material = new Material(TextureAtlas.LOCATION_BLOCKS, ClientSkinManager.getInstance().getSkinLocation(id));
        this.framerate = framerate;
    }

    public ResourceLocation getTextureLocation() {
        return this.material.texture();
    }
    
    public VertexConsumer buffer(MultiBufferSource renderTypeBuffer, Function<ResourceLocation, RenderType> renderType) {
        return material.buffer(renderTypeBuffer, renderType);
    }
    
    public void setupAnimation() {
        float index = framerate == 0  || this.getFrameCount() == 0
                ? 0 : (System.currentTimeMillis() / (1000L / framerate) % this.getFrameCount());
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.scalef(1, 1f / this.getFrameCount(), 1);
        RenderSystem.translatef(0, index, 0);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }
    
    public void endAnimation() {
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void load(ResourceManager rm) throws IOException {
        if(ni != null)
            return;

        File skinFile = new File(ClientSkinManager.getInstance().getSkinsFolder(), "textures/" + skinFilename + ".png");

        if(skinId.equals("default") || !skinFile.exists()) {
            try(Resource iresource = rm.getResource(DEFAULT_SKIN)) {
                ni = NativeImage.read(iresource.getInputStream());
            }
        } else {
            if(!skinFile.exists())
                return;
            ni = NativeImage.read(Files.newInputStream(skinFile.toPath()));
        }

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
        return ni == null ? 0 : (ni.getHeight() / ni.getWidth());
    }
}