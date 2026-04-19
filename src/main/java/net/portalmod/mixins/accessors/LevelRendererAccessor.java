package net.portalmod.mixins.accessors;

import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("lastViewDistance")
    int pmGetLastViewDistance();

    @Accessor("lastViewDistance")
    void pmSetLastViewDistance(int v);
}
