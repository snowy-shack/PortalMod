package net.portalmod.common.sorted.portalgun;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.VanillaProxy;

public class PortalGunGeometry extends VanillaProxy {
    public PortalGunGeometry(List<BlockPart> list) {
        super(list);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new PortalGunBakedModel(super.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }
    
    public static class Loader implements IModelLoader<VanillaProxy> {
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
        @Override
        public PortalGunGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            List<BlockPart> list = this.getModelElements(deserializationContext, modelContents);
            return new PortalGunGeometry(list);
        }

        private List<BlockPart> getModelElements(JsonDeserializationContext deserializationContext, JsonObject object) {
            List<BlockPart> list = Lists.newArrayList();
            if (object.has("elements"))
                for(JsonElement jsonelement : JSONUtils.getAsJsonArray(object, "elements"))
                    list.add(deserializationContext.deserialize(jsonelement, BlockPart.class));
            return list;
        }
    }
}