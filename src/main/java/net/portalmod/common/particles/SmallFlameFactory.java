package net.portalmod.common.particles;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.portalmod.mixins.accessors.FlameParticleAccessor;

import javax.annotation.Nullable;

public class SmallFlameFactory implements IParticleFactory<BasicParticleType> {
    private final IAnimatedSprite sprite;

    public SmallFlameFactory(IAnimatedSprite sprite) {
        this.sprite = sprite;
    }

    @Nullable
    @Override
    public Particle createParticle(BasicParticleType basicParticleType, ClientWorld world, double a, double b, double c, double d, double e, double f) {
        FlameParticle flameParticle = FlameParticleAccessor.pmInit(world, a, b, c, d, e, f);
        flameParticle.pickSprite(this.sprite);
        flameParticle.scale(0.5f);
        return flameParticle;
    }
}
