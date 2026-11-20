package net.portalmod.common.sorted.cube.storage;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeRenderer;
import net.portalmod.common.sorted.cube.companion.CompanionCubeGlowLayer;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class StorageCubeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/storage_cube.png");
    
    public StorageCubeRenderer(EntityRendererProvider.Context erm) {
        super(erm);
        this.addLayer(new StorageCubeGlowLayer<>(this));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}