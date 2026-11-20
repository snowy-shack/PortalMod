package net.portalmod.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class DynamicTextureVertexBuilder implements VertexConsumer {
    private final VertexConsumer real;
    private float offsetU;
    private float offsetV;

    public DynamicTextureVertexBuilder(VertexConsumer real) {
        this.real = real;
    }

    public void setOffset(float u, float v) {
        this.offsetU = u;
        this.offsetV = v;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return real.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        return real.color(r, g, b, a);
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        return real.uv(u + offsetU, v + offsetV);
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return real.overlayCoords(u, v);
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return real.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return real.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        real.endVertex();
    }
}