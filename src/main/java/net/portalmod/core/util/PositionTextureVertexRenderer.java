package net.portalmod.core.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.portalmod.client.render.Shader;
import net.portalmod.core.math.Mat4;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public class PositionTextureVertexRenderer {
    private final int primitive;
    private VertexBuffer vb;

    public PositionTextureVertexRenderer(int primitive) {
        this.primitive = primitive;
    }

    public void reset() {
        if(this.vb != null)
            this.vb.close();
        this.vb = new VertexBuffer(DefaultVertexFormats.POSITION_TEX);
    }

    public void data(Consumer<BufferBuilder> data) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(this.primitive, DefaultVertexFormats.POSITION_TEX);
        data.accept(bufferbuilder);
        bufferbuilder.end();
        this.vb.upload(bufferbuilder);
    }

    public void render(Shader shader) {
        this.vb.bind();

        int positionLocation = shader.getAttributeLocation("position");
        int uvLocation = shader.getAttributeLocation("uv");

        if(positionLocation >= 0) {
            GL30.glEnableVertexAttribArray(positionLocation);
            GL30.glVertexAttribPointer(positionLocation, 3, GL11.GL_FLOAT, false, 20, 0);
        }

        if(uvLocation >= 0) {
            GL30.glEnableVertexAttribArray(uvLocation);
            GL30.glVertexAttribPointer(uvLocation, 2, GL11.GL_FLOAT, false, 20, 12);
        }

        this.vb.draw(Mat4.identity().to4f(), this.primitive);

        if(positionLocation >= 0) {
            GL30.glDisableVertexAttribArray(positionLocation);
        }

        if(uvLocation >= 0) {
            GL30.glDisableVertexAttribArray(uvLocation);
        }

        VertexBuffer.unbind();
    }
}