package net.portalmod.mixins.accessors;

import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Queue;

@Mixin(ChunkRenderDispatcher.class)
public interface ChunkRenderDispatcherAccessor {
    @Accessor(value = "freeBuffers")
    Queue<RegionRenderCacheBuilder> pmGetFreeBuffers();
}