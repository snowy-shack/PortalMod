package net.portalmod.common.sorted.longfallboots;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.portalmod.core.init.ItemInit;

/**
 * Draws the long fall boots with their own model.
 * Forge used Item#getArmorModel/getArmorTexture; Fabric uses an ArmorRenderer.
 */
@Environment(EnvType.CLIENT)
public class LongFallBootsClient {
    private static LongFallBootsModel model;

    public static void register() {
        ArmorRenderer.register((matrices, buffers, stack, entity, slot, light, contextModel) -> {
            if (model == null)
                model = new LongFallBootsModel();
            model.copyLegsFrom(contextModel);
            ArmorRenderer.renderPart(matrices, buffers, light, stack, model, LongFallBoots.TEXTURE);
        }, ItemInit.LONGFALL_BOOTS.get());
    }
}
