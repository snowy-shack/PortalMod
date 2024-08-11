package net.portalmod.common.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.init.ParticleInit;

import java.util.Random;

public class PortalGunSparkParticle extends SpriteTexturedParticle {

    public static final Random RANDOM = new Random();

    public int u;
    public int v;

    protected PortalGunSparkParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz);
        this.lifetime = 2;
        this.gravity = 0;
        this.yd -= 0.1;

        this.u = RANDOM.nextInt(4);
        this.v = RANDOM.nextInt(4);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected float getU0() {
        return this.sprite.getU(this.u * 4);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU((this.u + 1) * 4);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.v * 4);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.v + 1) * 4);
    }

    public static void createParticles(World world, PlayerEntity player) {
        Vector3d viewLocation = player.getEyePosition(1).add(player.getViewVector(1).scale(1.5));

        // 1 - 3
        int amount = RANDOM.nextInt(3) + 1;

        for (int i = 0; i < amount; i++) {
            Vector3d offset = new Vector3d(symmetricRandom(0.3), symmetricRandom(0.3), symmetricRandom(0.3));
            Vector3d particlePos = viewLocation.add(offset);

            world.addParticle(ParticleInit.PORTALGUN_SPARK.get(), particlePos.x, particlePos.y, particlePos.z, symmetricRandom(0.5), symmetricRandom(0.5), symmetricRandom(0.5));
        }
    }

    public static double symmetricRandom(double i) {
        return (RANDOM.nextFloat() - 0.5) * 2 * i;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite p_i50823_1_) {
            this.sprite = p_i50823_1_;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            PortalGunSparkParticle particle = new PortalGunSparkParticle(world, x, y, z, dx, dy, dz);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
