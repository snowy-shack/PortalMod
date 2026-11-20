package net.portalmod.common.sorted.button;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;

/**
 * Wraps the super button's JSON model and bakes one rotated copy per facing and corner.
 * Forge did this with a custom geometry loader; on Fabric the model loading API hands us the
 * loaded JSON model (see PortalModModels) and we wrap it.
 */
@Environment(EnvType.CLIENT)
public class SuperButtonUnbakedModel implements UnbakedModel {
    private final UnbakedModel inner;

    public SuperButtonUnbakedModel(UnbakedModel inner) {
        this.inner = inner;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return inner.getDependencies();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        inner.resolveParents(resolver);
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state, ResourceLocation location) {
        SuperButtonBakedModel model = new SuperButtonBakedModel(inner.bake(baker, sprites, state, location));
        for (Direction facing : Direction.values())
            for (QuadBlockCorner corner : QuadBlockCorner.values())
                model.addVariant(facing, corner, inner.bake(baker, sprites, new SuperButtonModelTransform(facing, corner), location));
        return model;
    }
}
