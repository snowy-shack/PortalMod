package net.portalmod.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

/** Maps the raw GL primitive ids the 1.16 code used to 1.17+ VertexFormat.Mode values. */
public final class GlModes {
    private GlModes() {}

    public static VertexFormat.Mode fromGl(int glMode) {
        return switch (glMode) {
            case GL11.GL_QUADS -> VertexFormat.Mode.QUADS;
            case GL11.GL_TRIANGLES -> VertexFormat.Mode.TRIANGLES;
            case GL11.GL_TRIANGLE_STRIP -> VertexFormat.Mode.TRIANGLE_STRIP;
            case GL11.GL_TRIANGLE_FAN -> VertexFormat.Mode.TRIANGLE_FAN;
            case GL11.GL_LINES -> VertexFormat.Mode.DEBUG_LINES;
            case GL11.GL_LINE_STRIP -> VertexFormat.Mode.DEBUG_LINE_STRIP;
            default -> throw new IllegalArgumentException("Unsupported GL primitive " + glMode);
        };
    }
}
