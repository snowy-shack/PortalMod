package net.portalmod.mixins.accessors;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

@Mixin(SplashManager.class)
public interface SplashesAccessor {
    @Mutable
    @Accessor(value = "SPLASHES_LOCATION")
    static void pmSetLocation(ResourceLocation value) {
        throw new AssertionError();
    }

    @Accessor(value = "SPLASHES_LOCATION")
    static ResourceLocation pmGetLocation() {
        throw new AssertionError();
    }
    
    @Invoker(value = "prepare")
    List<String> pmPrepare(ResourceManager resourceManager, ProfilerFiller profiler);
    
    @Invoker(value = "apply")
    void pmApply(List<String> list, ResourceManager resourceManager, ProfilerFiller profiler);
}