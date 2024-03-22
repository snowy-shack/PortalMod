package net.portalmod.common.sorted.cube.companion;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeRenderer;

public class CompanionCubeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/companion_cube.png");
    
    public CompanionCubeRenderer(EntityRendererManager erm) {
        super(erm);
        this.addLayer(new CompanionCubeGlowLayer<>(this));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}