package net.portalmod.common.sorted.superbutton;

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
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.VanillaProxy;

public class SuperButtonGeometry extends VanillaProxy {
    public SuperButtonGeometry(List<BlockPart> list) {
        super(list);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
            IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        
        TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));
        SuperButtonBakedModel bakedModel;
        
        {
            IModelBuilder<?> builder = IModelBuilder.of(owner, overrides, particle);
            addQuads(owner, builder, bakery, spriteGetter, modelTransform, modelLocation);
            bakedModel = new SuperButtonBakedModel(builder.build());
        }

        for(Direction facing : Direction.values()) {
            for(QuadBlockCorner corner : QuadBlockCorner.values()) {
                IModelTransform variantTransform = new SuperButtonModelTransform(facing, corner);
                IModelBuilder<?> builder = IModelBuilder.of(owner, overrides, particle);
                addQuads(owner, builder, bakery, spriteGetter, variantTransform, modelLocation);
                IBakedModel variant = builder.build();
                bakedModel.addVariant(facing, corner, variant);
            }
        }
        
        return bakedModel;
    }
    
    public static class Loader implements IModelLoader<VanillaProxy> {

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
        @Override
        public SuperButtonGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            List<BlockPart> list = this.getModelElements(deserializationContext, modelContents);
            return new SuperButtonGeometry(list);
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