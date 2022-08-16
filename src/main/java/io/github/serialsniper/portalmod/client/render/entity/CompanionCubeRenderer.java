package io.github.serialsniper.portalmod.client.render.entity;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.entities.AbstractCube;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CompanionCubeRenderer extends AbstractCubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/companion_cube.png");
    
    public CompanionCubeRenderer(EntityRendererManager erm) {
        super(erm);
    }
    
    @Override
    public ResourceLocation getTextureLocation(AbstractCube cube) {
        return TEXTURE;
    }
}