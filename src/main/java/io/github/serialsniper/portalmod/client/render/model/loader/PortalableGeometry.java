package io.github.serialsniper.portalmod.client.render.model.loader;

import com.mojang.datafixers.util.*;
import io.github.serialsniper.portalmod.client.render.model.PortalableBakedModel;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.geometry.*;

import java.util.*;
import java.util.function.*;

public class PortalableGeometry implements IModelGeometry<PortalableGeometry> {
    private ItemCameraTransforms itemCameraTransforms;

    public PortalableGeometry(ItemCameraTransforms itemCameraTransforms) {
        this.itemCameraTransforms = itemCameraTransforms;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new PortalableBakedModel(itemCameraTransforms);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singletonList(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, PortalableBakedModel.BASE));
    }
}