package net.portalmod.common.sorted.cube.vintage;

import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeRenderer;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class VintageCubeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/vintage_cube.png");

    public VintageCubeRenderer(EntityRenderDispatcher erm) {
        super(erm);
    }

    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}