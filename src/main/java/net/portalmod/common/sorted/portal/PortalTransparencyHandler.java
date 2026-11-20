package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.ChunkRenderDispatcherAccessor;
import net.portalmod.mixins.accessors.CompiledChunkAccessor;

import java.util.*;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

public class PortalTransparencyHandler {
    public static final RenderType.CompositeState PORTAL_TRANSLUCENT_STATE = RenderType.CompositeState.builder()
            .setShadeModelState(new RenderStateShard.ShadeModelStateShard(true))
            .setLightmapState(new RenderStateShard.LightmapStateShard(true))
            .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true))
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1.1)))
            .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }))
            .setOutputState(new RenderStateShard.OutputStateShard("translucent_target", () -> {
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
                }

            }, () -> {
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }

            }))
            .createCompositeState(true);

    public static final RenderType PORTAL_TRANSLUCENT = RenderType.create("portal_translucent", DefaultVertexFormat.BLOCK, 7, 262144, true, true, PORTAL_TRANSLUCENT_STATE);

    private static final BufferBuilder bufferBuilder = new BufferBuilder(262144);

    public static void resortTransparency(Camera portalCamera) {
        LevelRenderer lr = Minecraft.getInstance().levelRenderer;
        Queue<ChunkBufferBuilderPack> freeBuffers = ((ChunkRenderDispatcherAccessor)lr.chunkRenderDispatcher).pmGetFreeBuffers();
        if(!freeBuffers.isEmpty()) {
            for(LevelRenderer.RenderChunkInfo lric : lr.renderChunks) {
                net.minecraft.world.phys.Vec3 vector3d = portalCamera.getPosition();
                float x = (float)vector3d.x;
                float y = (float)vector3d.y;
                float z = (float)vector3d.z;
                CompiledChunkAccessor cca = ((CompiledChunkAccessor)lric.chunk.compiled.get());
                BufferBuilder.State bufferbuilder$state = cca.pmGetTransparencyState();
                if(bufferbuilder$state != null && cca.pmGetHasBlocks().contains(RenderType.translucent())) {
                    bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
                    bufferBuilder.restoreState(bufferbuilder$state);
                    bufferBuilder.sortQuads(x - (float) lric.chunk.getOrigin().getX(), y - (float) lric.chunk.getOrigin().getY(), z - (float) lric.chunk.getOrigin().getZ());
                    bufferBuilder.end();
                    lric.chunk.getBuffer(PortalTransparencyHandler.PORTAL_TRANSLUCENT).upload(bufferBuilder);
                }
            }
        }
    }

    public static void resortMainTransparency(Vec3 position) {
        LevelRenderer lr = Minecraft.getInstance().levelRenderer;
        Queue<ChunkBufferBuilderPack> freeBuffers = ((ChunkRenderDispatcherAccessor)lr.chunkRenderDispatcher).pmGetFreeBuffers();
        if(!freeBuffers.isEmpty()) {
            for(LevelRenderer.RenderChunkInfo lric : lr.renderChunks) {
                float x = (float)position.x;
                float y = (float)position.y;
                float z = (float)position.z;
                CompiledChunkAccessor cca = ((CompiledChunkAccessor)lric.chunk.compiled.get());
                BufferBuilder.State bufferbuilder$state = cca.pmGetTransparencyState();
                if(bufferbuilder$state != null && cca.pmGetHasBlocks().contains(RenderType.translucent())) {
                    bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
                    bufferBuilder.restoreState(bufferbuilder$state);
                    bufferBuilder.sortQuads(x - (float) lric.chunk.getOrigin().getX(), y - (float) lric.chunk.getOrigin().getY(), z - (float) lric.chunk.getOrigin().getZ());
                    bufferBuilder.end();
                    lric.chunk.getBuffer(RenderType.translucent()).upload(bufferBuilder);
                }
            }
        }
    }
}