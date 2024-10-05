package net.portalmod.common.sorted.sign;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.PortalMod;

public class ChamberSignRenderer extends EntityRenderer<ChamberSignEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/chamber_sign/chamber_sign.png");

    public final ChamberSignModel model = new ChamberSignModel();

    public ChamberSignRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(ChamberSignEntity entity, float rotation, float b, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i) {
        matrixStack.pushPose();

        // Flip for whatever reason
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));

        // Rotate
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotation + 180));

        matrixStack.translate(0, -1.5, 0);

        this.model.changeModel(entity);
        this.model.renderToBuffer(matrixStack, renderTypeBuffer.getBuffer(this.model.renderType(TEXTURE)), LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        matrixStack.popPose();

        super.render(entity, rotation, b, matrixStack, renderTypeBuffer, i);
    }

    @Override
    public ResourceLocation getTextureLocation(ChamberSignEntity entity) {
        return TEXTURE;
    }
}
