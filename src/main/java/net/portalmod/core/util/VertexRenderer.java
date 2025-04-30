package net.portalmod.core.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.portalmod.core.math.Mat4;

import java.util.function.Consumer;

public class VertexRenderer {
    private final VertexFormat format;
    private final int mode;
    private VertexBuffer vb;

    public VertexRenderer(VertexFormat format, int mode) {
        this.format = format;
        this.mode = mode;
    }

    public void reset() {
        if(this.vb != null)
            this.vb.close();
        this.vb = new VertexBuffer(this.format);
    }

    public void data(Consumer<BufferBuilder> data) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(this.mode, this.format);
        data.accept(bufferbuilder);
        bufferbuilder.end();
        this.vb.upload(bufferbuilder);
    }

    public void render(Mat4 mat) {
        this.vb.bind();
        this.format.setupBufferState(0L);
        this.vb.draw(mat.to4f(), this.mode);
        VertexBuffer.unbind();
        this.format.clearBufferState();
    }

    public void render() {
        this.render(Mat4.identity());
    }
}