package net.portalmod.common.sorted.antline;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.IModelLoader;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.server.packs.resources.ResourceManager;

public class AntlineLoader implements IModelLoader<AntlineGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public AntlineGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
//        ItemTransforms itemCameraTransforms;
//        JsonObject transformsObject = modelContents.getAsJsonObject("display");
//        itemCameraTransforms = deserializationContext.deserialize(transformsObject, ItemTransforms.class);
//        return new AntlineGeometry(itemCameraTransforms);
        return new AntlineGeometry();
    }
}