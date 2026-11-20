package net.portalmod.common.sorted.portalgun.skins;

import net.minecraft.client.gui.narration.NarrationElementOutput;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.PortalMod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

public class SkinEntryWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/skinselector.png");

    private final SkinSelectorScreen parent;
    private final SkinPreviewWidget skinPreviewWidget;
    private final PortalGunSkin skin;
    private boolean selected;

    public SkinEntryWidget(int x, int y, int width, int height, SkinSelectorScreen parent, SkinPreviewWidget skinPreviewWidget, PortalGunSkin skin) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        this.skinPreviewWidget = skinPreviewWidget;
        this.skin = skin;
    }

    public PortalGunSkin getSkin() {
        return this.skin;
    }

    public void setSelected(boolean selected, boolean animate) {
        this.selected = selected;

        if(selected) {
            this.skinPreviewWidget.setSelectedSkin(this.skin.skin_id, animate);
        }
    }

    protected void renderBackground(GuiGraphics graphics) {
        PoseStack matrixStack = graphics.pose();
        if(this.selected) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, TEXTURE);
            graphics.blit(TEXTURE, this.getX(), this.getY(), 0, 176, this.width, this.height, 512, 512);
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        this.renderBackground(graphics);
        Font font = Minecraft.getInstance().font;

        String credit = (this.skin.artist != null) ? "- by " + this.skin.artist : "";

        graphics.drawString( font, "§l" + this.skin.name + " §r" + credit, this.getX() + 7, this.getY() + 7, 16777215);
        graphics.drawString( font, this.skin.description, this.getX() + 7, this.getY() + 7 + 10, 16777215);
    }

    @Override
    public void onClick(double x, double y) {
        this.parent.selectEntry(this, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
