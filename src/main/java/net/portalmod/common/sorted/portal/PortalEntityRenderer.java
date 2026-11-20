package net.portalmod.common.sorted.portal;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.portalmod.core.math.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class PortalEntityRenderer extends EntityRenderer<PortalEntity> {
    public static final float OFFSET = .0001f;
    
    public PortalEntityRenderer(EntityRendererProvider.Context p_i46166_1_) {
        super(p_i46166_1_);
    }
    
    public void render(PortalEntity portal, float a, float partialTicks, PoseStack matrixStack, MultiBufferSource renderBuffer, int b) {
        super.render(portal, a, partialTicks, matrixStack, renderBuffer, b);
    }

    public ResourceLocation getTextureLocation(PortalEntity p_110775_1_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
    
    protected boolean shouldShowName(PortalEntity portal) {
        return portal.hasCustomName();
    }
    
    protected void renderNameTag(PortalEntity portal, Component text, PoseStack matrixStack, MultiBufferSource renderBuffer, int light) {
        matrixStack.pushPose();
        net.minecraft.world.phys.Vec3 down = new Vec3(portal.getUpVector().getNormal()).mul(-1).to3d();
        matrixStack.translate(down.x, down.y, down.z);
        net.minecraft.world.phys.Vec3 forwards = new Vec3(portal.getDirection().getNormal()).mul(.2).to3d();
        matrixStack.translate(forwards.x, forwards.y, forwards.z);
        super.renderNameTag(portal, text, matrixStack, renderBuffer, light);
        matrixStack.popPose();
    }
}