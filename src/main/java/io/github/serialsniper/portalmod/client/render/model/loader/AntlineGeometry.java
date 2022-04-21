package io.github.serialsniper.portalmod.client.render.model.loader;

import com.mojang.datafixers.util.Pair;
import io.github.serialsniper.portalmod.client.render.model.AntlineBakedModel;
import io.github.serialsniper.portalmod.client.render.model.PortalableBakedModel;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class AntlineGeometry implements IModelGeometry<AntlineGeometry> {

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new AntlineBakedModel();
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singletonList(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, AntlineBakedModel.inactive("particle")));
    }
}