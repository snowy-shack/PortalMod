package net.portalmod.client.render;

import com.mojang.blaze3d.vertex.IVertexBuilder;

public class DynamicTextureVertexBuilder implements IVertexBuilder {
    private final IVertexBuilder real;
    private float offsetU;
    private float offsetV;

    public DynamicTextureVertexBuilder(IVertexBuilder real) {
        this.real = real;
    }

    public void setOffset(float u, float v) {
        this.offsetU = u;
        this.offsetV = v;
    }

    @Override
    public IVertexBuilder vertex(double x, double y, double z) {
        return real.vertex(x, y, z);
    }

    @Override
    public IVertexBuilder color(int r, int g, int b, int a) {
        return real.color(r, g, b, a);
    }

    @Override
    public IVertexBuilder uv(float u, float v) {
        return real.uv(u + offsetU, v + offsetV);
    }

    @Override
    public IVertexBuilder overlayCoords(int u, int v) {
        return real.overlayCoords(u, v);
    }

    @Override
    public IVertexBuilder uv2(int u, int v) {
        return real.uv2(u, v);
    }

    @Override
    public IVertexBuilder normal(float x, float y, float z) {
        return real.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        real.endVertex();
    }
}