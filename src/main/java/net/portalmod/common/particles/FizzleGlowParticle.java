package net.portalmod.common.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.init.ParticleInit;

import java.util.Random;

public class FizzleGlowParticle extends SpriteTexturedParticle {
    protected FizzleGlowParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz);
        this.lifetime = (int)(Math.random() * 6) + 3;
        this.gravity = 0;
        this.yd -= 0.1;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static void createGlowParticles(World world, LivingEntity entity) {
        double heightMid = entity.getBbHeight() * 0.5;
        double widthMid = entity.getBbWidth() * 0.5;

        Vector3d middle = entity.position().add(0, heightMid, 0);

        for (int i = 0; i < 20; i++) {
            Vector3d offset = new Vector3d(random(widthMid), random(heightMid), random(widthMid));
            Vector3d particlePos = middle.add(offset);

            world.addParticle(ParticleInit.FIZZLE_GLOW.get(), particlePos.x, particlePos.y, particlePos.z, offset.x * 0.7, offset.y * 0.7, offset.z * 0.7);
        }
    }

    public static double random(double i) {
        return (new Random().nextFloat() - 0.5) * 2 * i;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite p_i50823_1_) {
            this.sprite = p_i50823_1_;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            FizzleGlowParticle flameparticle = new FizzleGlowParticle(world, x, y, z, dx, dy, dz);
            flameparticle.pickSprite(this.sprite);
            return flameparticle;
        }
    }
}
