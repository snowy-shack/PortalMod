package net.portalmod.common.sorted.sign;

import net.portalmod.PortalMod;
import net.portalmod.common.sorted.faithplate.FaithPlateBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class ChamberSignRenderer extends EntityRenderer<ChamberSignEntity> {
    public static final ResourceLocation TEXTURE_ON = new ResourceLocation(PortalMod.MODID, "textures/entity/chamber_sign/chamber_sign_on.png");
    public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(PortalMod.MODID, "textures/entity/chamber_sign/chamber_sign_off.png");

    public final ChamberSignModel model = new ChamberSignModel();

    public ChamberSignRenderer(EntityRenderDispatcher manager) {
        super(manager);
    }

    @Override
    public void render(ChamberSignEntity entity, float rotation, float b, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int i) {
        boolean enabled = entity.getEnabled();

        matrixStack.pushPose();

        // Flip for whatever reason
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));

        // Rotate
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotation + 180));

        // Move down + z-fight fix
        matrixStack.translate(0, -1.505, -0.005);

        int light = enabled ? LightTexture.pack(15, 15) : LevelRenderer.getLightColor(entity.level, entity.getPos().above());

        this.model.changeModel(entity);
        this.model.renderToBuffer(matrixStack, renderTypeBuffer.getBuffer(this.model.renderType(enabled ? TEXTURE_ON : TEXTURE_OFF)),
                light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        matrixStack.popPose();

        super.render(entity, rotation, b, matrixStack, renderTypeBuffer, i);
    }

    @Override
    public ResourceLocation getTextureLocation(ChamberSignEntity entity) {
        return TEXTURE_ON;
    }
}
