package net.portalmod.common.sorted.faithplate;

import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.PortalMod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class FaithplateCheckboxButton extends AbstractButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/checkbox.png");
    private boolean ticked;
    private boolean unavailable;

    // new CheckboxButton(getX() + 230, getY() + 25 + verticalOffset, 20, 20, new StringTextComponent("Enabled"), be.isEnabled())
    public FaithplateCheckboxButton(int x, int y, int width, int height, Component textOn, boolean ticked) {
        super(x, y, width, height, textOn);
        this.ticked = ticked;
    }

    public void onPress() {
        if (this.unavailable) return;
        this.ticked = !this.ticked;
    }

    public boolean selected() {
        return this.ticked;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(TEXTURE);
        RenderSystem.enableDepthTest();
        Font fontRenderer = minecraft.font;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        blit(matrixStack, this.x, this.y, this.unavailable ? 20.0F : 0.0F,
                this.ticked ? 20.0F : 0.0F, 20, this.height, 64, 64);

        drawString(matrixStack, fontRenderer, this.getMessage(),
                this.x + 24, this.y + (this.height - 8) / 2, unavailable ? 0x7e7e7e : 0xFFFFFF);
    }
}
