package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.ITextComponent;
import net.portalmod.core.math.Vec3;

public class PortalEntityRenderer extends EntityRenderer<PortalEntity> {
    public static final float OFFSET = .0001f;
    
    public PortalEntityRenderer(EntityRendererManager p_i46166_1_) {
        super(p_i46166_1_);
    }
    
    public void render(PortalEntity portal, float a, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int b) {
        super.render(portal, a, partialTicks, matrixStack, renderBuffer, b);
    }

    public ResourceLocation getTextureLocation(PortalEntity p_110775_1_) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
    
    protected boolean shouldShowName(PortalEntity portal) {
        return portal.hasCustomName();
    }
    
    protected void renderNameTag(PortalEntity portal, ITextComponent text, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light) {
        matrixStack.pushPose();
        Vector3d down = new Vec3(portal.getUpVector().getNormal()).mul(-.5).to3d();
        matrixStack.translate(down.x, down.y, down.z);
        Vector3d forwards = new Vec3(portal.getDirection().getNormal()).mul(.2).to3d();
        matrixStack.translate(forwards.x, forwards.y, forwards.z);
        super.renderNameTag(portal, text, matrixStack, renderBuffer, light);
        matrixStack.popPose();
    }
}