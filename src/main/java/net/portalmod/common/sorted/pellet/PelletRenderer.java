package net.portalmod.common.sorted.pellet;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

public class PelletRenderer extends EntityRenderer<PelletEntity> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/energy_pellet.png");

    public final PelletModel model = new PelletModel();

    public PelletRenderer(EntityRendererManager entityRendererManager) {
        super(entityRendererManager);
    }

    @Override
    public void render(PelletEntity pelletEntity, float rotation, float idk, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i) {

        this.model.renderToBuffer(matrixStack, renderTypeBuffer.getBuffer(RenderType.entityTranslucent(TEXTURE)), LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        super.render(pelletEntity, rotation, idk, matrixStack, renderTypeBuffer, i);
    }

    @Override
    public ResourceLocation getTextureLocation(PelletEntity pelletEntity) {
        return TEXTURE;
    }
}
