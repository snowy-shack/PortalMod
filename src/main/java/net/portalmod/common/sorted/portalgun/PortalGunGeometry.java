package net.portalmod.common.sorted.portalgun;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.VanillaProxy;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public class PortalGunGeometry extends VanillaProxy {
    public PortalGunGeometry(List<BlockElement> list) {
        super(list);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new PortalGunBakedModel(super.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }
    
    public static class Loader implements IModelLoader<VanillaProxy> {
        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {}
        
        @Override
        public PortalGunGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            List<BlockElement> list = this.getModelElements(deserializationContext, modelContents);
            return new PortalGunGeometry(list);
        }

        private List<BlockElement> getModelElements(JsonDeserializationContext deserializationContext, JsonObject object) {
            List<BlockElement> list = Lists.newArrayList();
            if (object.has("elements"))
                for(JsonElement jsonelement : GsonHelper.getAsJsonArray(object, "elements"))
                    list.add(deserializationContext.deserialize(jsonelement, BlockElement.class));
            return list;
        }
    }
}