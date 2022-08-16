package io.github.serialsniper.portalmod.client.render.model;

import com.google.common.collect.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.client.model.*;

import javax.annotation.*;
import java.util.*;

public class PortalGunModel implements IBakedModel {
//    private static final RenderType SPIDER_EYES = RenderType.eyes(new ResourceLocation("textures/entity/spider_eyes.png"));
    private IBakedModel model;

    public PortalGunModel(IBakedModel model) {
//        IModelConfiguration config = new PortalGunModelConfiguration();
//
//        ItemMultiLayerBakedModel.Builder mlmodel;
//
//        mlmodel = ItemMultiLayerBakedModel.builder(config, originalModel.getParticleIcon(), originalModel.getOverrides(),
//                ImmutableMap.of(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, TransformationMatrix.identity()));
//
//        mlmodel.addQuads(SPIDER_EYES, originalModel.getQuads(null, null, new Random()));
//
//        model = mlmodel.build();
        this.model = model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random) {
        return model.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return model.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return model.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return model.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return model.getParticleIcon();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return model.getOverrides();
    }


//    public IBakedModel getModel() {
//        return model;
//    }

//    private class PortalGunModelConfiguration implements IModelConfiguration {
//        @Nullable
//        @Override
//        public IUnbakedModel getOwnerModel() {
//            return null;
//        }
//
//        @Override
//        public String getModelName() {
//            return null;
//        }
//
//        @Override
//        public boolean isTexturePresent(String name) {
//            return false;
//        }
//
//        @Override
//        public RenderMaterial resolveTexture(String name) {
//            return null;
//        }
//
//        @Override
//        public boolean isShadedInGui() {
//            return true;
//        }
//
//        @Override
//        public boolean isSideLit() {
//            return true;
//        }
//
//        @Override
//        public boolean useSmoothLighting() {
//            return true;
//        }
//
//        @Override
//        public ItemCameraTransforms getCameraTransforms() {
//            return null;
//        }
//
//        @Override
//        public IModelTransform getCombinedTransform() {
//            return null;
//        }
//    }
}