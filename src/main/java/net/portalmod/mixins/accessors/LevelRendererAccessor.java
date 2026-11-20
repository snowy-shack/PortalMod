package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("lastViewDistance")
    int pmGetLastViewDistance();

    @Accessor("lastViewDistance")
    void pmSetLastViewDistance(int v);
}
