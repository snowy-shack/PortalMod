package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public interface CompiledChunkAccessor {
    @Accessor(value = "transparencyState")
    BufferBuilder.SortState pmGetTransparencyState();

    @Accessor(value = "hasBlocks")
    Set<RenderType> pmGetHasBlocks();
}