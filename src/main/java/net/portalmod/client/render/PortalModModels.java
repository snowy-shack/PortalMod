package net.portalmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.antline.AntlineBakedModel;
import net.portalmod.common.sorted.button.QuadBlockCorner;
import net.portalmod.common.sorted.button.SuperButtonBakedModel;
import net.portalmod.common.sorted.button.SuperButtonModelTransform;

/**
 * Custom block models, registered with Fabric's model loading API.
 * (Forge used "loader" entries in the model JSON; vanilla ignores that key.)
 *
 * The JSON models are loaded and baked by vanilla as usual (item models use them as parents,
 * and vanilla only accepts plain JSON models as parents); afterwards the baked result is
 * replaced with the mod's own baked model.
 */
@Environment(EnvType.CLIENT)
public final class PortalModModels {
    private static final ResourceLocation ANTLINE = new ResourceLocation(PortalMod.MODID, "block/antline");
    private static final ResourceLocation SUPER_BUTTON = new ResourceLocation(PortalMod.MODID, "block/super_button");
    private static final ResourceLocation SUPER_BUTTON_PRESSED = new ResourceLocation(PortalMod.MODID, "block/super_button_pressed");

    private PortalModModels() {}

    public static void initialize(ModelLoadingPlugin.Context context) {
        context.modifyModelAfterBake().register((model, ctx) -> {
            ResourceLocation id = ctx.id();

            // Antline geometry is generated in code from the block entity's data.
            if (ANTLINE.equals(id))
                return new AntlineBakedModel();

            // The super button keeps its JSON geometry but needs a rotated copy per facing/corner.
            if (SUPER_BUTTON.equals(id) || SUPER_BUTTON_PRESSED.equals(id)) {
                SuperButtonBakedModel button = new SuperButtonBakedModel(model);
                for (Direction facing : Direction.values()) {
                    for (QuadBlockCorner corner : QuadBlockCorner.values()) {
                        BakedModel variant = ctx.sourceModel().bake(ctx.baker(), ctx.textureGetter(),
                                new SuperButtonModelTransform(facing, corner), id);
                        button.addVariant(facing, corner, variant);
                    }
                }
                return button;
            }
            return model;
        });
    }
}
