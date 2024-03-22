package net.portalmod.common.sorted.cube.storage;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeRenderer;
import net.portalmod.common.sorted.cube.companion.CompanionCubeGlowLayer;

public class StorageCubeRenderer extends CubeRenderer {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/cube/storage_cube.png");
    
    public StorageCubeRenderer(EntityRendererManager erm) {
        super(erm);
        this.addLayer(new StorageCubeGlowLayer<>(this));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Cube cube) {
        return TEXTURE;
    }
}