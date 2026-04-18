package net.portalmod.mixins.accessors;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkManager.class)
public interface ChunkManagerAccessor2 {
    @Invoker(value = "checkerboardDistance")
    static int pmCheckerboardDistance(ChunkPos pos, int x, int z) {
        throw new AssertionError();
    }
}