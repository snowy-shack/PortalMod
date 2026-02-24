package net.portalmod.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.portalmod.PortalMod;

public class WatermarkRenderer {
    public static final ResourceLocation WM_LEFT = new ResourceLocation(PortalMod.MODID, "textures/gui/watermark/playtester_left.png");
    public static final ResourceLocation WM_RIGHT = new ResourceLocation(PortalMod.MODID, "textures/gui/watermark/playtester_right.png");

    public static void render(MatrixStack matrixStack) {
        MainWindow window = Minecraft.getInstance().getWindow();
        int width = 128;
        int height = 16;

        RenderSystem.disableBlend();
        Minecraft.getInstance().getTextureManager().bind(WM_LEFT);
        blit(matrixStack, 0, 0, 0,
                0, 0, width, height, width, height);

        Minecraft.getInstance().getTextureManager().bind(WM_RIGHT);
        blit(matrixStack, window.getGuiScaledWidth() - width, window.getGuiScaledHeight() - height, 0,
                0, 0, width, height, width, height);
        RenderSystem.enableBlend();
    }

    private static void blit(MatrixStack matrixStack, int x, int y, int z, float u0, float v0, int uw, int uh, int width, int height) {
        innerBlit(matrixStack,
                x, x + uw,
                y, y + uh,
                z,
                uw, uh, u0, v0,
                width, height);
    }

    private static void innerBlit(MatrixStack matrixStack, int x0, int x1, int y0, int y1, int z, int uw, int uh, float u0, float v0, int width, int height) {
        innerBlit(matrixStack.last().pose(),
                x0, x1, y0, y1, z,
                (u0 + 0) / (float)width,
                (u0 + uw) / (float)width,
                (v0 + 0) / (float)height,
                (v0 + uh) / (float)height);
    }

    private static void innerBlit(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix, (float)x0, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v0).endVertex();
        bufferbuilder.vertex(matrix, (float)x0, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v0).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end(bufferbuilder);
    }
}