package net.portalmod.common.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.init.ParticleInit;

import java.util.Random;

public class FizzleFlakeParticle extends SpriteTexturedParticle {

    public FizzleFlakeParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz);
        this.setSize(0.01F, 0.01F);
        this.setColor(0.1f, 0.1f, 0.1f);
        this.gravity = 0.06F;
        this.yd -= 0.1;
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.preMoveUpdate();
        if (!this.removed) {
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.postMoveUpdate();
            if (!this.removed) {
                this.xd *= 0.98F;
                this.yd *= 0.98F;
                this.zd *= 0.98F;
            }
        }
    }

    public void preMoveUpdate() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }

    }

    public void postMoveUpdate() {
    }

    public static void createFlakeParticles(World world, LivingEntity entity) {
        double heightMid = entity.getBbHeight() * 0.5;
        double widthMid = entity.getBbWidth() * 0.5;

        Vector3d offset = new Vector3d(random(widthMid), random(heightMid), random(widthMid));
        Vector3d particlePos = entity.position().add(offset);

        world.addParticle(ParticleInit.FIZZLE_FLAKE_FALLING.get(), particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
    }

    public static double random(double i) {
        return (new Random().nextFloat() - 0.5) * 2 * i;
    }

    public static class FallingParticle extends FizzleFlakeParticle {
        public final IParticleData landParticle;

        public FallingParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz, IParticleData landParticle) {
            super(world, x, y, z, dx, dy, dz);
            this.landParticle = landParticle;
        }

        public void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static class FallingFactory implements IParticleFactory<BasicParticleType> {
        public final IAnimatedSprite sprite;

        public FallingFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            FizzleFlakeParticle particle = new FallingParticle(world, x, y, z, dx, dy, dz, ParticleInit.FIZZLE_FLAKE_LANDING.get());
            particle.pickSprite(this.sprite);
            return particle;
        }
    }

    public static class LandingParticle extends FizzleFlakeParticle {
        public LandingParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            super(world, x, y, z, dx, dy, dz);
            this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
        }
    }

    public static class LandingFactory implements IParticleFactory<BasicParticleType> {
        public final IAnimatedSprite sprite;

        public LandingFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            FizzleFlakeParticle particle = new FizzleFlakeParticle.LandingParticle(world, x, y, z, dx, dy, dz);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
