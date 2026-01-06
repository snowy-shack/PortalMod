package net.portalmod.skins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.portalmod.PortalMod;

public class SkinEntryWidget extends Widget {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/skinselector.png");

    private final SkinSelectorScreen parent;
    private final SkinPreviewWidget skinPreviewWidget;
    private final PortalGunSkin skin;
    private boolean selected;

    public SkinEntryWidget(int x, int y, int width, int height, SkinSelectorScreen parent, SkinPreviewWidget skinPreviewWidget, PortalGunSkin skin) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.parent = parent;
        this.skinPreviewWidget = skinPreviewWidget;
        this.skin = skin;
    }

    public void setSelected(boolean selected, boolean animate) {
        this.selected = selected;

        if(selected) {
            this.skinPreviewWidget.setSelectedSkin(this.skin.skin_id, animate);
        }
    }

    protected void renderBackground(MatrixStack matrixStack) {
        if(this.selected) {
            RenderSystem.color4f(1, 1, 1, 1);
            Minecraft.getInstance().getTextureManager().bind(TEXTURE);
            blit(matrixStack, this.x, this.y, 0, 176, this.width, this.height, 512, 512);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        FontRenderer font = Minecraft.getInstance().font;
        drawString(matrixStack, font, "§l" + this.skin.name, this.x + 7, this.y + 7, 16777215);
        drawString(matrixStack, font, this.skin.description, this.x + 7, this.y + 7 + 10, 16777215);
    }

    @Override
    public void onClick(double x, double y) {
        this.parent.selectEntry(this);
    }
}