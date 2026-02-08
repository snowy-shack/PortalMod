package net.portalmod.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class IconButton extends Button {
    private static final int SIZE = 20;
    private final ResourceLocation texture;
    private final int u;
    private final int v;

    public IconButton(int x, int y, ResourceLocation texture, int u, int v, IPressable onClick) {
        super(x, y, SIZE, SIZE, StringTextComponent.EMPTY, onClick);
        this.texture = texture;
        this.u = u;
        this.v = v;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if(this.visible) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bind(this.texture);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            blit(matrixStack, this.x + 2, this.y + 2, this.u, this.v + (this.active ? 0 : 16), 16, 16, 512, 512);
        }
    }
}