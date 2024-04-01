package net.portalmod.common.sorted.cube;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

public class GabeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/gabe.png");

    public GabeRenderer(EntityRendererManager erm) {
        super(erm);
    }

    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}