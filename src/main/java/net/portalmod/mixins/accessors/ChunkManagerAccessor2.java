package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ChunkMap;

@Mixin(ChunkMap.class)
public interface ChunkManagerAccessor2 {
    @Invoker(value = "checkerboardDistance")
    static int pmCheckerboardDistance(ChunkPos pos, int x, int z) {
        throw new AssertionError();
    }
}