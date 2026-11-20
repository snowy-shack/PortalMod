package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.portalmod.core.math.Vec3;

/**
 * Translucent geometry seen through portals.
 *
 * TODO(port): the 1.16 version re-sorted every visible chunk's translucent buffer from the portal
 * camera by reaching into the chunk renderer (BufferBuilder.State, CompiledChunk). 1.18 rewrote the
 * chunk renderer (sorting is now an async task per RenderChunk), so the re-sort is disabled until the
 * portal renderer is redesigned for 1.20.1. Translucent blocks seen through portals may sort wrongly.
 */
@Environment(EnvType.CLIENT)
public class PortalTransparencyHandler {
    public static final RenderType PORTAL_TRANSLUCENT = Types.PORTAL_TRANSLUCENT;

    public static void resortTransparency(Camera portalCamera) {
        // TODO(port): see class comment.
    }

    public static void resortMainTransparency(Vec3 position) {
        // TODO(port): see class comment.
    }

    /** RenderType.CompositeState is protected, so build the type from inside a RenderType subclass. */
    private abstract static class Types extends RenderType {
        static final RenderType PORTAL_TRANSLUCENT = RenderType.create("portal_translucent",
                DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, true, true,
                RenderType.CompositeState.builder()
                        .setLightmapState(LIGHTMAP)
                        .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(TRANSLUCENT_TARGET)
                        .createCompositeState(true));

        private Types(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean crumbling,
                      boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, crumbling, sort, setup, clear);
        }
    }
}
