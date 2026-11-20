package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(FlameParticle.class)
public interface FlameParticleAccessor {
    @Invoker(value = "<init>")
    static FlameParticle pmInit(ClientLevel world, double a, double b, double c, double d, double e, double f) {
        throw new AssertionError();
    }
}