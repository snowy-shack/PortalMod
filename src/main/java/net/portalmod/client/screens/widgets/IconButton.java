package net.portalmod.client.screens.widgets;

import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends Button {
    private static final int SIZE = 20;
    private final ResourceLocation texture;
    private final int u;
    private final int v;

    public IconButton(int x, int y, ResourceLocation texture, int u, int v, IPressable onClick) {
        super(x, y, SIZE, SIZE, Component.empty(), onClick);
        this.texture = texture;
        this.u = u;
        this.v = v;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if(this.visible) {
            Minecraft minecraft = Minecraft.getInstance();
            RenderSystem.setShaderTexture(0, this.texture);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            blit(matrixStack, this.getX() + 2, this.getY() + 2, this.u, this.v + (this.active ? 0 : 16), 16, 16, 512, 512);
        }
    }
}