package net.portalmod.common.sorted.cube.companion;

import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeRenderer;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class CompanionCubeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/companion_cube.png");
    
    public CompanionCubeRenderer(EntityRenderDispatcher erm) {
        super(erm);
        this.addLayer(new CompanionCubeGlowLayer<>(this));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}