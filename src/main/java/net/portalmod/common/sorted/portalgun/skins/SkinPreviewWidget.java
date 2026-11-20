package net.portalmod.common.sorted.portalgun.skins;

import net.minecraft.client.gui.narration.NarrationElementOutput;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import com.mojang.math.Axis;
import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.common.sorted.portalgun.PortalGunISTER;
import net.portalmod.common.sorted.portalgun.PortalGunModel;
import net.portalmod.common.sorted.portalgun.PortalGunModelManager;
import net.portalmod.core.util.Colour;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class SkinPreviewWidget extends AbstractWidget {
    private final SkinSelectorScreen parent;

    private boolean dragging;
    private float mousePrevX;
    private float mousePrevY;
    private long mousePrevTime;

    private float xRot;
    private float yRot;
    private float zRot;
    private float xRotOld;
    private float yRotOld;

    private static final float MOMENTUM_LIMIT = 200;
    private float xRotMomentum;
    private float yRotMomentum;
    private boolean clockwise;

    private long animationStart = -1;
    private boolean animationHalfLifePassed;
    private String selectedSkin;
    private String nextSelectedSkin;

    public SkinPreviewWidget(int x, int y, int width, int height, SkinSelectorScreen parent) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;

        this.xRot = 30;
        this.yRot = -135;
        this.xRotOld = this.xRot;
        this.yRotOld = this.yRot;
        this.xRotMomentum = 0;
        this.yRotMomentum = 0;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        partialTicks = Minecraft.getInstance().getFrameTime();
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        Colour lastPortalColor = new Colour(64, 59, 75, 255);
        Colour stripeColour = new Colour(255, 255, 255, 0);
        Colour tint = Optional.ofNullable(parent.getSkinTint()).orElse(Colour.WHITE);

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);

        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef((float)this.getX() + this.width / 2f, (float)this.getY() + this.width / 2f, 100.0F);
        RenderSystem.scalef(5, 5, 5);
        RenderSystem.scalef(16.0F, -16.0F, 16.0F);
        Lighting.setupFor3DItems();

        matrixStack.pushPose();

        this.computeAnimation();
        matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.xRotOld, this.xRot)));
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, this.yRotOld, this.yRot)));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(this.zRot));
        matrixStack.translate(0, -.2, 0);

        PortalGunISTER.renderGun(matrixStack, null, this.getModel(), irendertypebuffer$impl,
                ClientSkinManager.getInstance().getSkinTexture(selectedSkin),
                stripeColour, lastPortalColor, tint, false, false, 15728880, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

    public void tick() {
        this.xRotOld = this.xRot;
        this.yRotOld = this.yRot;

        if(this.dragging) {
            this.stopRotation();
        } else {
            this.xRot += this.xRotMomentum;
            this.yRot += this.yRotMomentum;
            this.xRot = (this.xRot - 30) * 0.99f + 30;

            float idleSpeed = (this.clockwise ? -1 : 1) * 4;
            this.yRotMomentum = (this.yRotMomentum - idleSpeed) * 0.8f + idleSpeed;
            this.xRotMomentum *= 0.5f;
        }

        this.wrapYRot();
        this.clampXRot();
    }

    private void setPrevInput(double x, double y) {
        this.mousePrevX = (float)x;
        this.mousePrevY = (float)y;
        this.mousePrevTime = System.currentTimeMillis();
    }

    private void stopRotation() {
        this.xRotMomentum = 0;
        this.yRotMomentum = 0;
    }

    private void wrapYRot() {
        while(this.yRot >= 360) {
            this.yRot -= 360;
            this.yRotOld -= 360;
        }

        while(this.yRot < 0) {
            this.yRot += 360;
            this.yRotOld += 360;
        }
    }

    private void clampXRot() {
        this.xRot = Mth.clamp(this.xRot, -90, 90);
    }

    private float getDeltaTicks() {
        return Math.max((System.currentTimeMillis() - this.mousePrevTime) / 50f, 0.001f);
    }

    private PortalGunModel getModel() {
        return PortalGunModelManager.getInstance().getModel(null);
    }

    private void computeAnimation() {
        if(this.animationStart == -1)
            return;

        float delta = (System.currentTimeMillis() - this.animationStart) / 1000f;

        if(delta > 1) {
            this.zRot = 0;
            this.selectedSkin = this.nextSelectedSkin;
            return;
        }

        if(delta > 0.15 && !this.animationHalfLifePassed) {
            this.selectedSkin = this.nextSelectedSkin;
            this.animationHalfLifePassed = true;
        }

        float factor = (float)(1 - Math.exp(-10 * delta));
        this.zRot = 360 * factor * factor;
    }

    public void setSelectedSkin(String skin, boolean animate) {
        this.nextSelectedSkin = skin;

        if(animate) {
            this.animationStart = System.currentTimeMillis();
            this.animationHalfLifePassed = false;
        } else {
            this.selectedSkin = this.nextSelectedSkin;
        }
    }

    @Override
    public void onClick(double x, double y) {
        this.dragging = true;
        this.setPrevInput(x, y);
        this.stopRotation();
    }

    public void mouseReleasedAnywhere(int button) {
        if(this.isValidClickButton(button)) {
            this.dragging = false;
        }
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);

        if(!this.dragging)
            return;

        float deltaXRot = (float)(y - this.mousePrevY);
        float deltaYRot = (float)(x - this.mousePrevX);

        this.xRotOld = this.xRot += deltaXRot;
        this.yRotOld = this.yRot += deltaYRot;
        this.wrapYRot();
        this.clampXRot();

        float delta = this.getDeltaTicks();
        this.xRotMomentum = Mth.clamp(deltaXRot / delta, -MOMENTUM_LIMIT, MOMENTUM_LIMIT);
        this.yRotMomentum = Mth.clamp(deltaYRot / delta, -MOMENTUM_LIMIT, MOMENTUM_LIMIT);

        if(this.yRotMomentum != 0) {
            this.clockwise = this.yRotMomentum < 0;
        }

        this.setPrevInput(x, y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
