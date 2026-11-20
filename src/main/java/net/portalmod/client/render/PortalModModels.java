package net.portalmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.antline.AntlineBakedModel;
import net.portalmod.common.sorted.button.SuperButtonUnbakedModel;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * Custom block models, registered with Fabric's model loading API.
 * (Forge used "loader" entries in the model JSON; vanilla ignores that key.)
 */
@Environment(EnvType.CLIENT)
public final class PortalModModels {
    private static final ResourceLocation ANTLINE = new ResourceLocation(PortalMod.MODID, "block/antline");
    private static final ResourceLocation SUPER_BUTTON = new ResourceLocation(PortalMod.MODID, "block/super_button");
    private static final ResourceLocation SUPER_BUTTON_PRESSED = new ResourceLocation(PortalMod.MODID, "block/super_button_pressed");

    private PortalModModels() {}

    public static void initialize(ModelLoadingPlugin.Context context) {
        // Antline geometry is generated in code from the block entity's data.
        context.resolveModel().register(ctx -> ANTLINE.equals(ctx.id()) ? new CodeModel(AntlineBakedModel::new) : null);

        // The super button keeps its JSON geometry but needs a rotated copy per facing/corner.
        context.modifyModelOnLoad().register((model, ctx) -> {
            ResourceLocation id = ctx.id();
            if (SUPER_BUTTON.equals(id) || SUPER_BUTTON_PRESSED.equals(id))
                return new SuperButtonUnbakedModel(model);
            return model;
        });
    }

    /** An unbaked model whose baked form is built entirely in code. */
    private record CodeModel(java.util.function.Supplier<BakedModel> factory) implements UnbakedModel {
        @Override
        public Collection<ResourceLocation> getDependencies() {
            return Collections.emptyList();
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {}

        @Override
        public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state, ResourceLocation location) {
            return factory.get();
        }
    }
}
