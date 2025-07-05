package net.portalmod.core.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;

public class DebugRenderer {
    private static final HashMap<String, Tuple<VoxelShape, Color>> SHAPES = new HashMap<>();
    private static final VertexBuffer debugVG = new VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
    private static final BufferBuilder builder = new BufferBuilder(7 * 240000);

    public static void putShape(String key, VoxelShape shape, Color color) {
        SHAPES.put(key, new Tuple<>(shape, color));
    }

    public static void removeShape(String key) {
        SHAPES.remove(key);
    }

    public static void renderAllShapes(MatrixStack matrixStack) {
        HashMap<String, Tuple<VoxelShape, Color>> temp = new HashMap<>(SHAPES);

        for(Tuple<VoxelShape, Color> shape : temp.values())
            for(AxisAlignedBB aabb : shape.getA().toAabbs())
                renderBox(matrixStack.last().pose(), aabb, shape.getB());
    }

    private static void renderBox(Matrix4f matrix, AxisAlignedBB aabb, Color color) {
        aabb = aabb.move(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().reverse());

        builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.maxY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.maxY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();

        builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.maxY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.maxY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();

        builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.maxY, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        builder.vertex(matrix, (float)aabb.minX, (float)aabb.maxY, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();

        float factor = 1 / 16f;

        for(int i = 1; i * factor < aabb.getXsize() + aabb.getYsize(); i++) {
            float x0 = i * factor;
            float y0 = 0;
            if(i * factor > aabb.getXsize()) {
                y0 = x0 - (float)aabb.getXsize();
                x0 = (float)aabb.getXsize();
            }

            float x1 = 0;
            float y1 = i * factor;
            if(i * factor > aabb.getYsize()) {
                x1 = y1 - (float)aabb.getYsize();
                y1 = (float)aabb.getYsize();
            }

            builder.vertex(matrix, (float)aabb.minX + x0, (float)aabb.minY + y0, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX + x1, (float)aabb.minY + y1, (float)aabb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX + x0, (float)aabb.minY + y0, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX + x1, (float)aabb.minY + y1, (float)aabb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        }

        for(int i = 1; i * factor < aabb.getZsize() + aabb.getYsize(); i++) {
            float z0 = i * factor;
            float y0 = 0;
            if(i * factor > aabb.getZsize()) {
                y0 = z0 - (float)aabb.getZsize();
                z0 = (float)aabb.getZsize();
            }

            float z1 = 0;
            float y1 = i * factor;
            if(i * factor > aabb.getYsize()) {
                z1 = y1 - (float)aabb.getYsize();
                y1 = (float)aabb.getYsize();
            }

            builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY + y0, (float)aabb.minZ + z0).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX, (float)aabb.minY + y1, (float)aabb.minZ + z1).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY + y0, (float)aabb.minZ + z0).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.maxX, (float)aabb.minY + y1, (float)aabb.minZ + z1).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        }

        for(int i = 1; i * factor < aabb.getXsize() + aabb.getZsize(); i++) {
            float x0 = i * factor;
            float z0 = 0;
            if(i * factor > aabb.getXsize()) {
                z0 = x0 - (float)aabb.getXsize();
                x0 = (float)aabb.getXsize();
            }

            float x1 = 0;
            float z1 = i * factor;
            if(i * factor > aabb.getZsize()) {
                x1 = z1 - (float)aabb.getZsize();
                z1 = (float)aabb.getZsize();
            }

            builder.vertex(matrix, (float)aabb.minX + x0, (float)aabb.minY, (float)aabb.minZ + z0).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX + x1, (float)aabb.minY, (float)aabb.minZ + z1).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX + x0, (float)aabb.maxY, (float)aabb.minZ + z0).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
            builder.vertex(matrix, (float)aabb.minX + x1, (float)aabb.maxY, (float)aabb.minZ + z1).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
        }

        builder.end();
        debugVG.upload(builder);

        debugVG.bind();
        DefaultVertexFormats.POSITION_COLOR.setupBufferState(0L);
        debugVG.draw(Matrix4f.createScaleMatrix(1, 1, 1), GL11.GL_LINES);
        DefaultVertexFormats.POSITION_COLOR.clearBufferState();
        VertexBuffer.unbind();
    }
}