//package io.github.serialsniper.portalmod.client.render.tileentity;
//
//import com.mojang.blaze3d.matrix.*;
//import com.mojang.blaze3d.systems.*;
//import io.github.serialsniper.portalmod.client.render.CubeCoordinates;
//import io.github.serialsniper.portalmod.common.tiles.StencilBoxTileEntity;
//import net.minecraft.client.*;
//import net.minecraft.client.renderer.*;
//import net.minecraft.client.renderer.texture.*;
//import net.minecraft.client.renderer.tileentity.*;
//import net.minecraft.client.renderer.vertex.*;
//import net.minecraft.util.*;
//import net.minecraft.util.math.vector.*;
//import net.minecraftforge.api.distmarker.*;
//import org.lwjgl.opengl.*;
//
//import javax.annotation.*;
//
//@OnlyIn(Dist.CLIENT)
//public class StencilBoxTER extends TileEntityRenderer<StencilBoxTileEntity> {
//    private static VertexBuffer buffer;
//
//    public StencilBoxTER(TileEntityRendererDispatcher terd) {
//        super(terd);
//    }
//
//    @Override
//    public void render(StencilBoxTileEntity stencilBoxTileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer,
//                       int combinedLight, int combinedOverlay) {
//
//        Minecraft.getInstance().getMainRenderTarget().enableStencil();
//
//        RenderSystem.enableDepthTest();
//        renderCube(matrixStack, CubeCoordinates.Facing.INSIDE, new Vector4f(0.7f, 0.7f, 0.7f, 1), 1, combinedLight);
//
//        drawStenciledCube(matrixStack, Direction.UP, new Vector4f(1, 0, 0, 1), 0.5f, combinedLight);
//        drawStenciledCube(matrixStack, Direction.WEST, new Vector4f(0, 1, 0, 1), 0.5f, combinedLight);
//        drawStenciledCube(matrixStack, Direction.SOUTH, new Vector4f(0, 0, 1, 1), 0.5f, combinedLight);
//    }
//
//    private void drawStenciledCube(MatrixStack matrixStack, Direction side, Vector4f color, float size, int combinedLight) {
//        RenderSystem.disableDepthTest();
//        setupStencil();
//        drawStencilMask(matrixStack, side);
//        applyStencil();
//        renderCube(matrixStack, CubeCoordinates.Facing.OUTSIDE, color, size, combinedLight);
//        clearStencil();
//        RenderSystem.enableDepthTest();
//    }
//
//    private void setupStencil() {
//        GL11.glEnable(GL11.GL_STENCIL_TEST);
////        RenderSystem.clearStencil(0);
//        RenderSystem.stencilMask(0xFF);
//        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
//    }
//
//    private void clearStencil() {
//        GL11.glDisable(GL11.GL_STENCIL_TEST);
//    }
//
//    private void drawStencilMask(MatrixStack transform, @Nonnull Direction side) {
//        RenderSystem.stencilMask(0xFF);
//        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
//        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
//
//        renderFace(transform, side, CubeCoordinates.Facing.OUTSIDE, new Vector4f(1, 1, 1, 0.5f), 1, false, 0);
//    }
//
//    private void applyStencil() {
//        RenderSystem.stencilMask(0x00);
//        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0, 0xFF);
//        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
//    }
//
//    private static void buildFace(@Nonnull Direction side, CubeCoordinates.Facing facing, Vector4f color, float d, boolean render, int combinedLight) {
//        float v1 = (1.0f - d) / 2.0f;
//        float v2 = 1.0f - v1;
//
//        if(color == null)
//            color = new Vector4f(1, 1, 1, 1);
//
//        Vector3f[] coords = CubeCoordinates.getQuad(side, facing, d);
//
//        if(buffer != null)
//            buffer.close();
//
//        VertexFormat format = DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP;
//
//        if(!render)
//            format = DefaultVertexFormats.POSITION;
//
//        buffer = new VertexBuffer(format);
//
//        BufferBuilder builder = Tessellator.getInstance().getBuilder();
//        builder.begin(7, format);
//
//        if(render) {
//            builder.vertex(coords[0].x(), coords[0].y(), coords[0].z()).color(color.x(), color.y(), color.z(), color.w()).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(1, 1, 1).endVertex();
//            builder.vertex(coords[1].x(), coords[1].y(), coords[1].z()).color(color.x(), color.y(), color.z(), color.w()).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(1, 1, 1).endVertex();
//            builder.vertex(coords[2].x(), coords[2].y(), coords[2].z()).color(color.x(), color.y(), color.z(), color.w()).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(1, 1, 1).endVertex();
//            builder.vertex(coords[3].x(), coords[3].y(), coords[3].z()).color(color.x(), color.y(), color.z(), color.w()).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(1, 1, 1).endVertex();
//        } else {
//            builder.vertex(coords[0].x(), coords[0].y(), coords[0].z()).endVertex();
//            builder.vertex(coords[1].x(), coords[1].y(), coords[1].z()).endVertex();
//            builder.vertex(coords[2].x(), coords[2].y(), coords[2].z()).endVertex();
//            builder.vertex(coords[3].x(), coords[3].y(), coords[3].z()).endVertex();
//        }
//
//        builder.end();
//
//        buffer.upload(builder);
//        buffer.bind();
//    }
//
//    private void renderFace(MatrixStack transform, @Nonnull Direction side, CubeCoordinates.Facing facing, Vector4f color, float d, boolean render, int combinedLight) {
//        VertexFormat format = DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP;
//
//        if(!render)
//            format = DefaultVertexFormats.POSITION;
//
//        buildFace(side, facing, color, d, render, combinedLight);
//
//        RenderSystem.enableCull();
//        RenderSystem.enableBlend();
//
//        if(!render)
//            RenderSystem.colorMask(false, false, false, false);
//
//        RenderSystem.bindTexture(0);
//
//        transform.pushPose();
//        format.setupBufferState(0L);
//        buffer.draw(transform.last().pose(), 7);
//        transform.popPose();
//
//        RenderSystem.colorMask(true, true, true, true);
//
//        RenderSystem.disableBlend();
//
//        format.clearBufferState();
//        VertexBuffer.unbind();
//    }
//
//    private void renderCube(MatrixStack transform, CubeCoordinates.Facing facing, Vector4f color, float dimension, int combinedLight) {
//        renderFace(transform, Direction.NORTH, facing, color, dimension, true, combinedLight);
//        renderFace(transform, Direction.SOUTH, facing, color, dimension, true, combinedLight);
//        renderFace(transform, Direction.WEST, facing, color, dimension, true, combinedLight);
//        renderFace(transform, Direction.EAST, facing, color, dimension, true, combinedLight);
//        renderFace(transform, Direction.UP, facing, color, dimension, true, combinedLight);
//        renderFace(transform, Direction.DOWN, facing, color, dimension, true, combinedLight);
//    }
//}