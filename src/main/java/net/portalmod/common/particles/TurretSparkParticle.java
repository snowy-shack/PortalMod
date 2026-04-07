package net.portalmod.common.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.init.ParticleInit;

import java.util.Random;

public class TurretSparkParticle extends SpriteTexturedParticle {

    public int u;
    public int v;

    protected TurretSparkParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, 0, 0, 0);
        this.lifetime = 3;
        this.gravity = 0;
    }

    @Override
    public IParticleRenderType getRenderType() { return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override
    protected int getLightColor(float idk) { return 15728880; } // See WorldRenderer:2714

    @Override
    public void tick() {
        // super.tick(); // That's what you fucking deserve.
        if (this.age++ >= this.lifetime) this.remove();

        this.u = this.age % 2;
        this.v = (int) Math.floor((double) this.age / 2);
    }

    @Override
    protected float getU0() {
        return this.sprite.getU(this.u * 8);
    }
    @Override
    protected float getU1() {
        return this.sprite.getU((this.u + 1) * 8);
    }
    @Override
    protected float getV0() {
        return this.sprite.getV(this.v * 8);
    }
    @Override
    protected float getV1() {
        return this.sprite.getV((this.v + 1) * 8);
    }

    public static void createGlowParticles(World world, LivingEntity entity, Vector3d direction) {
        double heightMid = entity.getBbHeight() * 0.5;
        Vector3d middle = entity.position().add(0, heightMid, 0);

        Vector3d front = middle.add(direction.normalize().multiply(0.25F, 0F, 0.25F));

        Vector3d side = new Vector3d(-direction.normalize().z, 0, direction.normalize().x);
//        boolean leftSide = new Random().nextBoolean();
//        boolean upperSide = new Random().nextBoolean();

        long tickTime = world.getGameTime() + entity.getId();
        boolean leftSide = tickTime % 8 < 4;
        boolean upperSide = tickTime % 4 < 2;

        float verticalOffset = upperSide ? 0.12F : 0;
        side = side.multiply(leftSide ? -0.25F : 0.25F, 0, leftSide ? -0.25F : 0.25F);

        Vector3d particlePos = front.add(0F, 0.08F, 0F);

        world.addParticle(ParticleInit.TURRET_SPARK.get(),
                particlePos.x + side.x, particlePos.y + verticalOffset, particlePos.z + side.z,
                0, 0, 0);
    }

    public static double random(double i) {
        return (new Random().nextFloat() - 0.5) * 2 * i;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite p_i50823_1_) { this.sprite = p_i50823_1_; }

        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
            TurretSparkParticle particle = new TurretSparkParticle(world, x, y, z, 0, 0, 0);
            particle.setSpriteFromAge(this.sprite);
            return particle;
        }
    }
}
