package net.portalmod.common.sorted.antline;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class AntlineLoader implements IModelLoader<AntlineGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public AntlineGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
//        ItemCameraTransforms itemCameraTransforms;
//        JsonObject transformsObject = modelContents.getAsJsonObject("display");
//        itemCameraTransforms = deserializationContext.deserialize(transformsObject, ItemCameraTransforms.class);
//        return new AntlineGeometry(itemCameraTransforms);
        return new AntlineGeometry();
    }
}