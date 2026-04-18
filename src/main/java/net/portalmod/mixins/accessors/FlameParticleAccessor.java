package net.portalmod.mixins.accessors;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlameParticle.class)
public interface FlameParticleAccessor {
    @Invoker(value = "<init>")
    static FlameParticle pmInit(ClientWorld world, double a, double b, double c, double d, double e, double f) {
        throw new AssertionError();
    }
}