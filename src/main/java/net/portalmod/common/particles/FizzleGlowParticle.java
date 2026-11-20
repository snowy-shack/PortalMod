package net.portalmod.common.particles;

import net.portalmod.core.init.ParticleInit;

import java.util.Random;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class FizzleGlowParticle extends TextureSheetParticle {
    protected FizzleGlowParticle(ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz);
        this.lifetime = (int)(Math.random() * 6) + 3;
        this.gravity = 0;
        this.yd -= 0.1;
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override
    protected int getLightColor(float idk) { return 15728880; } // See WorldRenderer:2714

    @Override
    public void tick() {
        super.tick();
    }

    public static void createGlowParticles(Level world, LivingEntity entity) {
        double heightMid = entity.getBbHeight() * 0.5;
        double widthMid = entity.getBbWidth() * 0.5;

        Vec3 middle = entity.position().add(0, heightMid, 0);

        for (int i = 0; i < 20; i++) {
            Vec3 offset = new Vec3(random(widthMid), random(heightMid), random(widthMid));
            Vec3 particlePos = middle.add(offset);

            world.addParticle(ParticleInit.FIZZLE_GLOW.get(), particlePos.x, particlePos.y, particlePos.z, offset.x * 0.5, offset.y * 0.5, offset.z * 0.5);
        }
    }

    public static double random(double i) {
        return (new Random().nextFloat() - 0.5) * 2 * i;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet p_i50823_1_) {
            this.sprite = p_i50823_1_;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
            FizzleGlowParticle flameparticle = new FizzleGlowParticle(world, x, y, z, dx, dy, dz);
            flameparticle.pickSprite(this.sprite);
            return flameparticle;
        }
    }
}
