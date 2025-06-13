package net.portalmod.mixins.accessors;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public interface CompiledChunkAccessor {
    @Accessor(remap = false, value = "transparencyState")
    BufferBuilder.State pmGetTransparencyState();

    @Accessor(remap = false, value = "hasBlocks")
    Set<RenderType> pmGetHasBlocks();
}