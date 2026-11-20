package net.portalmod.common.particles;

import net.portalmod.mixins.accessors.FlameParticleAccessor;

import javax.annotation.Nullable;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SmallFlameFactory implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprite;

    public SmallFlameFactory(SpriteSet sprite) {
        this.sprite = sprite;
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType basicParticleType, ClientLevel world, double a, double b, double c, double d, double e, double f) {
        FlameParticle flameParticle = FlameParticleAccessor.pmInit(world, a, b, c, d, e, f);
        flameParticle.pickSprite(this.sprite);
        flameParticle.scale(0.5f);
        return flameParticle;
    }
}
