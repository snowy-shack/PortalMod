package net.portalmod.mixins.accessors;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.util.Splashes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Mixin(Splashes.class)
public interface SplashesAccessor {
    @Accessor(value = "SPLASHES_LOCATION")
    static void pmSetLocation(ResourceLocation value) {
        throw new AssertionError();
    }

    @Accessor(value = "SPLASHES_LOCATION")
    static ResourceLocation pmGetLocation() {
        throw new AssertionError();
    }
    
    @Invoker(value = "prepare")
    List<String> pmPrepare(IResourceManager resourceManager, IProfiler profiler);
    
    @Invoker(value = "apply")
    void pmApply(List<String> list, IResourceManager resourceManager, IProfiler profiler);
}