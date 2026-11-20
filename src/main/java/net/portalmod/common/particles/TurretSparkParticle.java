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

public class TurretSparkParticle extends TextureSheetParticle {

    public int u;
    public int v;

    protected TurretSparkParticle(ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, 0, 0, 0);
        this.lifetime = 3;
        this.gravity = 0;
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
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

    public static void createGlowParticles(Level world, LivingEntity entity, Vec3 direction) {
        double heightMid = entity.getBbHeight() * 0.5;
        Vec3 middle = entity.position().add(0, heightMid, 0);

        Vec3 front = middle.add(direction.normalize().multiply(0.25F, 0F, 0.25F));

        Vec3 side = new Vec3(-direction.normalize().z, 0, direction.normalize().x);
//        boolean leftSide = new Random().nextBoolean();
//        boolean upperSide = new Random().nextBoolean();

        long tickTime = world.getGameTime() + entity.getId();
        boolean leftSide = tickTime % 8 < 4;
        boolean upperSide = tickTime % 4 < 2;

        float verticalOffset = upperSide ? 0.12F : 0;
        side = side.multiply(leftSide ? -0.25F : 0.25F, 0, leftSide ? -0.25F : 0.25F);

        Vec3 particlePos = front.add(0F, 0.08F, 0F);

        world.addParticle(ParticleInit.TURRET_SPARK.get(),
                particlePos.x + side.x, particlePos.y + verticalOffset, particlePos.z + side.z,
                0, 0, 0);
    }

    public static double random(double i) {
        return (new Random().nextFloat() - 0.5) * 2 * i;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet p_i50823_1_) { this.sprite = p_i50823_1_; }

        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
            TurretSparkParticle particle = new TurretSparkParticle(world, x, y, z, 0, 0, 0);
            particle.setSpriteFromAge(this.sprite);
            return particle;
        }
    }
}
