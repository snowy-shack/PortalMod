package net.portalmod.core.util;

import net.portalmod.client.render.GlModes;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.portalmod.client.render.Shader;
import net.portalmod.core.math.Mat4;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;

public class PositionTextureVertexRenderer {
    private final VertexFormat.Mode primitive;
    private VertexBuffer vb;

    public PositionTextureVertexRenderer(int primitive) {
        this.primitive = GlModes.fromGl(primitive);
    }

    public void reset() {
        if(this.vb != null)
            this.vb.close();
        this.vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
    }

    public void data(Consumer<BufferBuilder> data) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(this.primitive, DefaultVertexFormat.POSITION_TEX);
        data.accept(bufferbuilder);
        this.vb.bind();
        this.vb.upload(bufferbuilder.end());
        VertexBuffer.unbind();
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

        // 1.16 passed an identity model-view here; the mod's own shaders get their matrices as uniforms.
        this.vb.draw();

        if(positionLocation >= 0) {
            GL30.glDisableVertexAttribArray(positionLocation);
        }

        if(uvLocation >= 0) {
            GL30.glDisableVertexAttribArray(uvLocation);
        }

        VertexBuffer.unbind();
    }
}