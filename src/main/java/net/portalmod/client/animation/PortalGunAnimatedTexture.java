package net.portalmod.client.animation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portalgun.skins.SkinManager;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;

public class PortalGunAnimatedTexture extends Texture {
    private static final ResourceLocation DEFAULT_SKIN = new ResourceLocation(PortalMod.MODID, "textures/portalgun/default.png");
    private final String skinId;
    private final RenderMaterial material;
    private final int framerate;
    private NativeImage ni;
    
    public PortalGunAnimatedTexture(String id, int framerate) {
        this.skinId = id;
        this.material = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, SkinManager.getClientInstance().getSkinLocation(id));
        this.framerate = framerate;
    }

    public ResourceLocation getTextureLocation() {
        return this.material.texture();
    }
    
    public IVertexBuilder buffer(IRenderTypeBuffer renderTypeBuffer, Function<ResourceLocation, RenderType> renderType) {
        return material.buffer(renderTypeBuffer, renderType);
    }
    
    public void setupAnimation() {
        float height = material.sprite().getV1();
        float index = framerate == 0  || this.getFrameCount() == 0
                ? 0 : (System.currentTimeMillis() / (1000f / framerate) % this.getFrameCount());
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0, height / this.getFrameCount() * index, 0);
        RenderSystem.scalef(1, 1f / this.getFrameCount(), 1);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }
    
    public void endAnimation() {
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void load(IResourceManager rm) throws IOException {
        if(ni != null)
            return;

        File skinFile = new File(SkinManager.getClientInstance().getSkinsFolder(), "textures/" + skinId + ".png");

        if(skinId.equals("default") || !skinFile.exists()) {
            try(IResource iresource = rm.getResource(DEFAULT_SKIN)) {
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