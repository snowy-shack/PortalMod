package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.ChunkRenderDispatcherAccessor;
import net.portalmod.mixins.accessors.CompiledChunkAccessor;

import java.util.*;

public class PortalTransparencyHandler {
    public static final RenderType.State PORTAL_TRANSLUCENT_STATE = RenderType.State.builder()
            .setShadeModelState(new RenderState.ShadeModelState(true))
            .setLightmapState(new RenderState.LightmapState(true))
            .setTextureState(new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS, false, true))
            .setLineState(new RenderState.LineState(OptionalDouble.of(1.1)))
            .setTransparencyState(new RenderState.TransparencyState("translucent_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }))
            .setOutputState(new RenderState.TargetState("translucent_target", () -> {
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
                }

            }, () -> {
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }

            }))
            .createCompositeState(true);

    public static final RenderType PORTAL_TRANSLUCENT = RenderType.create("portal_translucent", DefaultVertexFormats.BLOCK, 7, 262144, true, true, PORTAL_TRANSLUCENT_STATE);

    private static final BufferBuilder bufferBuilder = new BufferBuilder(262144);

    public static void resortTransparency(ActiveRenderInfo portalCamera) {
        WorldRenderer lr = Minecraft.getInstance().levelRenderer;
        Queue<RegionRenderCacheBuilder> freeBuffers = ((ChunkRenderDispatcherAccessor)lr.chunkRenderDispatcher).pmGetFreeBuffers();
        if(!freeBuffers.isEmpty()) {
            for(WorldRenderer.LocalRenderInformationContainer lric : lr.renderChunks) {
                Vector3d vector3d = portalCamera.getPosition();
                float x = (float)vector3d.x;
                float y = (float)vector3d.y;
                float z = (float)vector3d.z;
                CompiledChunkAccessor cca = ((CompiledChunkAccessor)lric.chunk.compiled.get());
                BufferBuilder.State bufferbuilder$state = cca.pmGetTransparencyState();
                if(bufferbuilder$state != null && cca.pmGetHasBlocks().contains(RenderType.translucent())) {
                    bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
                    bufferBuilder.restoreState(bufferbuilder$state);
                    bufferBuilder.sortQuads(x - (float) lric.chunk.getOrigin().getX(), y - (float) lric.chunk.getOrigin().getY(), z - (float) lric.chunk.getOrigin().getZ());
                    bufferBuilder.end();
                    lric.chunk.getBuffer(PortalTransparencyHandler.PORTAL_TRANSLUCENT).upload(bufferBuilder);
                }
            }
        }
    }

    public static void resortMainTransparency(Vec3 position) {
        WorldRenderer lr = Minecraft.getInstance().levelRenderer;
        Queue<RegionRenderCacheBuilder> freeBuffers = ((ChunkRenderDispatcherAccessor)lr.chunkRenderDispatcher).pmGetFreeBuffers();
        if(!freeBuffers.isEmpty()) {
            for(WorldRenderer.LocalRenderInformationContainer lric : lr.renderChunks) {
                float x = (float)position.x;
                float y = (float)position.y;
                float z = (float)position.z;
                CompiledChunkAccessor cca = ((CompiledChunkAccessor)lric.chunk.compiled.get());
                BufferBuilder.State bufferbuilder$state = cca.pmGetTransparencyState();
                if(bufferbuilder$state != null && cca.pmGetHasBlocks().contains(RenderType.translucent())) {
                    bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
                    bufferBuilder.restoreState(bufferbuilder$state);
                    bufferBuilder.sortQuads(x - (float) lric.chunk.getOrigin().getX(), y - (float) lric.chunk.getOrigin().getY(), z - (float) lric.chunk.getOrigin().getZ());
                    bufferBuilder.end();
                    lric.chunk.getBuffer(RenderType.translucent()).upload(bufferBuilder);
                }
            }
        }
    }
}