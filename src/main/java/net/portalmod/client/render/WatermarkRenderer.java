package net.portalmod.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.PortalMod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class WatermarkRenderer {
    public static final ResourceLocation WM_LEFT = new ResourceLocation(PortalMod.MODID, "textures/gui/watermark/playtester_left.png");
    public static final ResourceLocation WM_RIGHT = new ResourceLocation(PortalMod.MODID, "textures/gui/watermark/playtester_right.png");

    public static void render(PoseStack matrixStack) {
        Window window = Minecraft.getInstance().getWindow();
        int width = 256;
        int height = 32;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();

        RenderSystem.setShaderTexture(0, WM_LEFT);
        blit(matrixStack, 0, 0, 0,
                0, 0, width, height, width, height);

        RenderSystem.setShaderTexture(0, WM_RIGHT);
        blit(matrixStack, window.getGuiScaledWidth() - width, 0, 0,
                0, 0, width, height, width, height);
    }

    private static void blit(PoseStack matrixStack, int x, int y, int z, float u0, float v0, int uw, int uh, int width, int height) {
        innerBlit(matrixStack,
                x, x + uw,
                y, y + uh,
                z,
                uw, uh, u0, v0,
                width, height);
    }

    private static void innerBlit(PoseStack matrixStack, int x0, int x1, int y0, int y1, int z, int uw, int uh, float u0, float v0, int width, int height) {
        innerBlit(matrixStack.last().pose(),
                x0, x1, y0, y1, z,
                (u0 + 0) / (float)width,
                (u0 + uw) / (float)width,
                (v0 + 0) / (float)height,
                (v0 + uh) / (float)height);
    }

    private static void innerBlit(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix, (float)x0, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v0).endVertex();
        bufferbuilder.vertex(matrix, (float)x0, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v0).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }
}