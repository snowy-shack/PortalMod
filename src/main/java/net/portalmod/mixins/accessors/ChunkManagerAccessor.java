package net.portalmod.mixins.accessors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkManager.class)
public interface ChunkManagerAccessor {
    @Accessor(value = "entityMap")
    Int2ObjectMap<?> pmGetEntityMap();

    @Accessor(value = "viewDistance")
    int pmGetViewDistance();

    @Invoker(value = "getVisibleChunkIfPresent")
    ChunkHolder pmGetVisibleChunkIfPresent(long pos);

    @Invoker(value = "checkerboardDistance")
    static int pmCheckerboardDistance(ChunkPos pos, ServerPlayerEntity player, boolean oldPos) {
        throw new AssertionError();
    }

    @Invoker(value = "getChunks")
    Iterable<ChunkHolder> pmGetChunks();
}