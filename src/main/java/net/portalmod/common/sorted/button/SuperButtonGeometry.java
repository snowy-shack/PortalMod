package net.portalmod.common.sorted.button;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.VanillaProxy;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public class SuperButtonGeometry extends VanillaProxy {
    public SuperButtonGeometry(List<BlockElement> list) {
        super(list);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        
        TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));
        SuperButtonBakedModel bakedModel;
        
        {
            IModelBuilder<?> builder = IModelBuilder.of(owner, overrides, particle);
            addQuads(owner, builder, bakery, spriteGetter, modelTransform, modelLocation);
            bakedModel = new SuperButtonBakedModel(builder.build());
        }

        for(Direction facing : Direction.values()) {
            for(QuadBlockCorner corner : QuadBlockCorner.values()) {
                ModelState variantTransform = new SuperButtonModelTransform(facing, corner);
                IModelBuilder<?> builder = IModelBuilder.of(owner, overrides, particle);
                addQuads(owner, builder, bakery, spriteGetter, variantTransform, modelLocation);
                BakedModel variant = builder.build();
                bakedModel.addVariant(facing, corner, variant);
            }
        }
        
        return bakedModel;
    }
    
    public static class Loader implements IModelLoader<VanillaProxy> {

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {}
        
        @Override
        public SuperButtonGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            List<BlockElement> list = this.getModelElements(deserializationContext, modelContents);
            return new SuperButtonGeometry(list);
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