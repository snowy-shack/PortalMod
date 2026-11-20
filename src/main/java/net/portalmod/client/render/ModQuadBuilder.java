package net.portalmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Builds a vanilla {@link BakedQuad} from four vertices (replaces Forge's BakedQuadBuilder).
 * Vertices are packed in DefaultVertexFormat.BLOCK order: position, colour, uv, light, normal.
 */
@Environment(EnvType.CLIENT)
public final class ModQuadBuilder {
    private static final int INTS_PER_VERTEX = 8;

    private final TextureAtlasSprite sprite;
    private final int[] data = new int[INTS_PER_VERTEX * 4];
    private Direction orientation = Direction.UP;
    private int vertex = 0;

    public ModQuadBuilder(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void setQuadOrientation(Direction orientation) {
        this.orientation = orientation;
    }

    /** u and v are already atlas coordinates (sprite.getU / getV applied). */
    public void vertex(double x, double y, double z, float u, float v, float r, float g, float b, Vec3 normal) {
        if (vertex >= 4)
            throw new IllegalStateException("A quad has only four vertices");
        int i = vertex * INTS_PER_VERTEX;
        data[i] = Float.floatToRawIntBits((float) x);
        data[i + 1] = Float.floatToRawIntBits((float) y);
        data[i + 2] = Float.floatToRawIntBits((float) z);
        data[i + 3] = 0xFF << 24 | to255(b) << 16 | to255(g) << 8 | to255(r); // ABGR
        data[i + 4] = Float.floatToRawIntBits(u);
        data[i + 5] = Float.floatToRawIntBits(v);
        data[i + 6] = 0; // light, filled in by the renderer
        data[i + 7] = packNormal(normal);
        vertex++;
    }

    public BakedQuad build() {
        return new BakedQuad(data.clone(), -1, orientation, sprite, true);
    }

    private static int to255(float c) {
        return Math.max(0, Math.min(255, Math.round(c * 255.0F)));
    }

    private static int packNormal(Vec3 n) {
        int x = (byte) Math.round(n.x * 127) & 0xFF;
        int y = (byte) Math.round(n.y * 127) & 0xFF;
        int z = (byte) Math.round(n.z * 127) & 0xFF;
        return x | y << 8 | z << 16;
    }
}
