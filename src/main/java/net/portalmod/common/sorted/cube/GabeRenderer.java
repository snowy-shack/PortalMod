package net.portalmod.common.sorted.cube;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.portalmod.PortalMod;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class GabeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/gabe.png");

    public GabeRenderer(EntityRendererProvider.Context erm) {
        super(erm);
    }

    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}