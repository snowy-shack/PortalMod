package net.portalmod.mixins.accessors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkManagerAccessor {
    @Accessor(value = "entityMap")
    Int2ObjectMap<?> pmGetEntityMap();

    @Accessor(value = "viewDistance")
    int pmGetViewDistance();

    @Invoker(value = "getVisibleChunkIfPresent")
    ChunkHolder pmGetVisibleChunkIfPresent(long pos);

    @Invoker(value = "getChunks")
    Iterable<ChunkHolder> pmGetChunks();
}
