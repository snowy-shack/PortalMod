package net.portalmod.common.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.sorted.portalgun.PortalGun;
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
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;

        this.u = RANDOM.nextInt(4);
        this.v = RANDOM.nextInt(4);
    }

    @Override
    public IParticleRenderType getRenderType() { return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override
    protected int getLightColor(float idk) { return 15728880; } // See WorldRenderer:2714

    @Override
    public void tick() {

//        this.setAlpha(1f - ((float) this.age / this.getLifetime()));

        if (this.age == 1) {
            this.setAlpha(0.5f);
        }

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
        Vector3d viewVec = player.getViewVector(1);
        Vector3d rightVec = new Vector3d(-viewVec.z, 0, viewVec.x).normalize(); // Perpendicular to view
        Vector3d downVec = new Vector3d(0, -1, 0); // Straight down

        Vector3d viewLocation = player.getEyePosition(1)
                .add(viewVec.scale(1)) // Forward
                .add(rightVec.scale(0.4)
                        .scale(player.getMainHandItem().getItem() instanceof PortalGun ? 1 : -1)) // Right
                .add(downVec.scale(0.2)); // Down

        int amount = RANDOM.nextInt(3) + 1; // 1 - 3
        double speed = 0.07;
        double spread = 0.3;

        for (int i = 0; i < amount; i++) {
            Vector3d offset = new Vector3d(symmetricRandom(spread), symmetricRandom(spread), symmetricRandom(spread));
            Vector3d particlePos = viewLocation.add(offset);

            world.addParticle(ParticleInit.PORTALGUN_SPARK.get(), particlePos.x, particlePos.y, particlePos.z, symmetricRandom(speed), symmetricRandom(speed), symmetricRandom(speed));
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
