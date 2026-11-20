package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Queue;

import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Mixin(ChunkRenderDispatcher.class)
public interface ChunkRenderDispatcherAccessor {
    @Accessor(value = "freeBuffers")
    Queue<ChunkBufferBuilderPack> pmGetFreeBuffers();
}