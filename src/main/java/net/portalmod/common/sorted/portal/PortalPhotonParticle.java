package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.portalmod.core.init.ParticleInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import java.awt.*;
import java.util.Random;

public class PortalPhotonParticle extends SpriteTexturedParticle {
    private final PortalEntity portal;
    private final double x0, y0, z0;
    private final double x1, y1, z1;
    private final float speed;
    private final float decay;
    private final float end;
    private final boolean smooth;

    protected PortalPhotonParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, PortalPhotonParticleData data, IAnimatedSprite sprite) {
        super(level, x, y, z, xd, yd, zd);

        this.portal = data.getPortal();

        this.x0 = x;
        this.y0 = y;
        this.z0 = z;

        this.x1 = xd;
        this.y1 = yd;
        this.z1 = zd;

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.speed = data.getSpeed();
        this.decay = data.getDecay();
        this.end = this.random.nextFloat() * 0.1f;
        this.smooth = data.isSmooth();
        this.calculateAlpha();

        this.lifetime = 20;
        this.gravity = 0;

        this.setSpriteFromAge(sprite);
        this.quadSize = 1/32f - (float)this.random.nextGaussian() * 0.005f;

        this.rCol = data.getRed();
        this.gCol = data.getGreen();
        this.bCol = data.getBlue();
    }

    @Override
    public void tick() {
        super.tick();

        if(this.portal != null && !this.portal.isAlive())
            this.remove();

        this.calculateAlpha();
        this.calculatePos();
    }

    private void calculateAlpha() {
        this.alpha = MathHelper.clamp((float)Math.pow((this.lifetime - this.age) / decay, 2), 0, 0.8f);
        if(this.smooth)
            this.alpha *= 1 - (float)Math.exp(-this.age / 7f);

        if(this.portal != null)
            this.alpha *= MathHelper.clamp((float)this.portal.getAge() / 30f * this.portal.getAge() / 30f, 0f, 1f);
    }

    private void calculatePos() {
        double f = 1 - Math.exp(-this.age / this.speed) - this.end;
        this.setPos(
                MathHelper.lerp(f, this.x0, this.x1),
                MathHelper.lerp(f, this.y0, this.y1),
                MathHelper.lerp(f, this.z0, this.z1)
        );
    }

    @Override
    public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float f) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        super.render(vertexBuilder, camera, f);
    }

    public static void createLivingParticles(PortalEntity portal) {
        Random random = new Random();

        for(int i = 0; i < 360; i += 20) {
            boolean doesRender = random.nextInt(100) < 30;
            if(!doesRender)
                continue;

            int deltaAngle = 40 * (portal.getEnd() == PortalEnd.PRIMARY ? 1 : -1);
            float randomAngle = (random.nextFloat() - .5f) * 4;
            float startAngle = (i + randomAngle) * (float)Math.PI / 180;
            float endAngle = (i + deltaAngle + randomAngle) * (float)Math.PI / 180;

            float randomRadius = (random.nextFloat() - .5f) * 0.1f;
            float startRadius = 0.55f + randomRadius;
            float endRadius = 0.45f + randomRadius;

            createRadialParticle(portal, random, startRadius, endRadius, startAngle, endAngle,
                    random.nextFloat() * 2 + 15,
                    random.nextFloat() * 18 + 15,
                    true, true);
        }
    }

    public static void createOpeningParticles(PortalEntity portal) {
        Random random = new Random();

        for(int i = 0; i < 360; i += 10) {
            for(int j = 0; j < 3; j++) {
                float randomAngle = (random.nextFloat() - .5f) * 4;
                float angle = (i + randomAngle) * (float)Math.PI / 180;

                float innerRadius = 0.2f;
                float outerRadius = 0.45f;
                float randomRadius = (random.nextFloat() - .5f) * 0.1f;
                float startRadius = 0;
                float endRadius = (outerRadius - innerRadius) * (j / 2f) + innerRadius + randomRadius;

                int decayBase = 0;
                switch(j) {
                    case 2: decayBase = 5;  break;
                    case 1: decayBase = 10; break;
                    case 0: decayBase = 22; break;
                }

                createRadialParticle(portal, random, startRadius, endRadius, angle, angle,
                        random.nextFloat() + 2,
                        random.nextFloat() * 18 + decayBase,
                        false, false);
            }
        }
    }

    public static void createClosingParticles(PortalEntity portal) {
        Random random = new Random();

        for(int i = 0; i < 360; i += 20) {
            float randomAngle = (random.nextFloat() - .5f) * 4;
            float angle = (i + randomAngle) * (float)Math.PI / 180;

            float randomRadius = (random.nextFloat() - .5f) * 0.1f;
            float startRadius = 0.45f + randomRadius;
            float endRadius = 0.1f + randomRadius;

            createRadialParticle(portal, random, startRadius, endRadius, angle, angle,
                    random.nextFloat() * 2 + 4,
                    random.nextFloat() * 18 + 30,
                    false, false);
        }
    }

    private static void createRadialParticle(PortalEntity portal, Random random, float startRadius, float endRadius, float startAngle, float endAngle, float speed, float decay, boolean smooth, boolean linked) {
        Mat4 modelMatrix = portal.getSourceBasis().getChangeOfBasisFromCanonicalMatrix();
        Color color = PortalColors.getInstance().getColor(portal);

        if(portal.level == null || color == null)
            return;

        float x0 = (float)Math.cos(startAngle);
        float y0 = (float)Math.sin(startAngle);
        float x1, y1;

        if(startAngle == endAngle) {
            x1 = x0;
            y1 = y0;
        } else {
            x1 = (float)Math.cos(endAngle);
            y1 = (float)Math.sin(endAngle);
        }

        x0 *= startRadius;
        y0 *= startRadius * 2;

        x1 *= endRadius;
        y1 *= endRadius * 2;

        boolean outwards = endRadius > startRadius;

        Vec3 pos0 = new Vec3(x0, y0, outwards ? 0 : 0.1).transform(modelMatrix).add(portal.position());
        Vec3 pos1 = new Vec3(x1, y1, outwards ? 0.1 : 0).transform(modelMatrix).add(portal.position());

        createParticle(portal, random, pos0, pos1, color, speed, decay, smooth, linked);
    }

    private static void createParticle(PortalEntity portal, Random random, Vec3 pos0, Vec3 pos1, Color color, float speed, float decay, boolean smooth, boolean linked) {
        float randomR = random.nextFloat() * 0.1f;
        float randomG = random.nextFloat() * 0.1f;
        float randomB = random.nextFloat() * 0.1f;

        portal.level.addParticle(
                new PortalPhotonParticleData(
                        linked ? portal : null,
                        MathHelper.clamp(color.getRed() / 255f   + randomR, 0, 1),
                        MathHelper.clamp(color.getGreen() / 255f + randomG, 0, 1),
                        MathHelper.clamp(color.getBlue() / 255f  + randomB, 0, 1),
                        speed,
                        decay,
                        smooth
                ),
                pos0.x, pos0.y, pos0.z,
                pos1.x, pos1.y, pos1.z
        );
    }

    @Override
    protected float getU0() {
        return this.sprite.getU(0);
    }
    
    @Override
    protected float getV0() {
        return this.sprite.getV(0);
    }
    
    @Override
    protected float getU1() {
        return this.sprite.getU(2);
    }
    
    @Override
    protected float getV1() {
        return this.sprite.getV(2);
    }
    
    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;
        
        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }
        
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            PortalPhotonParticle particle = new PortalPhotonParticle(level, x, y, z, xd, yd, zd, (PortalPhotonParticleData)type, sprite);
            particle.pickSprite(sprite);
            return particle;
        }
    }
    
    public static class PortalPhotonParticleData extends BasicParticleType {
        private final PortalEntity portal;
        private final float r, g, b;
        private final float speed;
        private final float decay;
        private final boolean smooth;

        public PortalPhotonParticleData(PortalEntity portal, float r, float g, float b, float speed, float decay, boolean smooth) {
            super(false);
            this.portal = portal;
            this.r = r;
            this.g = g;
            this.b = b;
            this.speed = speed;
            this.decay = decay;
            this.smooth = smooth;
        }

        @Override
        public BasicParticleType getType() {
            return ParticleInit.PORTAL_PHOTON.get();
        }

        public PortalEntity getPortal() {
            return this.portal;
        }

        public float getRed() {
            return this.r;
        }

        public float getGreen() {
            return this.g;
        }

        public float getBlue() {
            return this.b;
        }

        public float getSpeed() {
            return this.speed;
        }

        public float getDecay() {
            return this.decay;
        }

        public boolean isSmooth() {
            return this.smooth;
        }
    }
}