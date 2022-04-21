package io.github.serialsniper.portalmod.client.render.model;

import com.google.common.collect.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.client.model.*;

import javax.annotation.*;
import java.util.*;

public class PortalGunModel {
    private static final RenderType SPIDER_EYES = RenderType.eyes(new ResourceLocation("textures/entity/spider_eyes.png"));
    private IBakedModel model;

    public PortalGunModel(IBakedModel originalModel) {
        IModelConfiguration config = new PortalGunModelConfiguration();

        ItemMultiLayerBakedModel.Builder mlmodel;

        mlmodel = ItemMultiLayerBakedModel.builder(config, originalModel.getParticleIcon(), originalModel.getOverrides(),
                ImmutableMap.of(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, TransformationMatrix.identity()));

        mlmodel.addQuads(SPIDER_EYES, originalModel.getQuads(null, null, new Random()));

        model = mlmodel.build();
    }

    public IBakedModel getModel() {
        return model;
    }

    private class PortalGunModelConfiguration implements IModelConfiguration {
        @Nullable
        @Override
        public IUnbakedModel getOwnerModel() {
            return null;
        }

        @Override
        public String getModelName() {
            return null;
        }

        @Override
        public boolean isTexturePresent(String name) {
            return false;
        }

        @Override
        public RenderMaterial resolveTexture(String name) {
            return null;
        }

        @Override
        public boolean isShadedInGui() {
            return true;
        }

        @Override
        public boolean isSideLit() {
            return true;
        }

        @Override
        public boolean useSmoothLighting() {
            return true;
        }

        @Override
        public ItemCameraTransforms getCameraTransforms() {
            return null;
        }

        @Override
        public IModelTransform getCombinedTransform() {
            return null;
        }
    }
}