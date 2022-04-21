package io.github.serialsniper.portalmod.client.render.entity;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.render.model.AbstractCubeModel;
import io.github.serialsniper.portalmod.common.entities.CompanionCube;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;

public class CompanionCubeRenderer extends LivingRenderer<CompanionCube, AbstractCubeModel<CompanionCube>> {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/companion_cube.png");

    public CompanionCubeRenderer(EntityRendererManager erm) {
        super(erm, new AbstractCubeModel(), 0.5f);
    }

    @Override
    protected boolean shouldShowName(CompanionCube cube) {
        return cube.hasCustomName();
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionCube cube) {
        return TEXTURE;
    }
}