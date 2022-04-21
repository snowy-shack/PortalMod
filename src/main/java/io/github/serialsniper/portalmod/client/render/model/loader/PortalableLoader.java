package io.github.serialsniper.portalmod.client.render.model.loader;

import com.google.gson.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.resources.*;
import net.minecraftforge.client.model.*;

public class PortalableLoader implements IModelLoader<PortalableGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public PortalableGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ItemCameraTransforms itemCameraTransforms;
        JsonObject transformsObject = modelContents.getAsJsonObject("display");
        itemCameraTransforms = deserializationContext.deserialize(transformsObject, ItemCameraTransforms.class);

        return new PortalableGeometry(itemCameraTransforms);
    }
}