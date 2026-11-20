package net.portalmod.common.sorted.portal;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;

import net.fabricmc.api.Environment;
import net.portalmod.core.init.ParticleInit;

import java.util.Locale;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

public class PortalParticle extends TextureSheetParticle {
    private final int u, v;
    protected PortalParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, PortalParticleData data, SpriteSet sprite) {
        super(level, x, y, z, xd, yd, zd);

//        float min = .02f;
//
//        switch(data.direction.getAxis()) {
//            case X:
//                if(Math.abs(this.xd) < min)
//                    this.xd = Math.signum(data.direction.getStepX()) * min;
//                break;
//
//            case Y:
//                if(Math.abs(this.yd) < min)
//                    this.yd = Math.signum(data.direction.getStepY()) * min;
////                if(data.direction.getStepY() * yd <= 0)
////                    this.yd = -this.yd;
//                break;
//
//            case Z:
//                if(Math.abs(this.zd) < min)
//                    this.zd = Math.signum(data.direction.getStepZ()) * min;
////                if(data.direction.getStepZ() * zd <= 0)
////                    this.zd = -this.zd;
//                break;
//        }

//        this.xd = xd;
//        this.yd = yd;
//        this.zd = zd;
        this.lifetime = (int)(Math.random() * 10) + 10;
        this.gravity = 1.0F;

        this.setSpriteFromAge(sprite);
        this.quadSize /= 2f;
        this.u = this.random.nextInt(4);
        this.v = this.random.nextInt(4);

        this.rCol = data.getRed();
        this.gCol = data.getGreen();
        this.bCol = data.getBlue();
        this.alpha = data.getAlpha();
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = Mth.clamp((float)Math.pow((this.lifetime - this.age) / 7f, 2), 0, 1);

        this.oRoll = this.roll;
        Vec3 delta = new Vec3(this.xd, this.yd, this.zd);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        delta.xRot(camera.getXRot()).yRot(camera.getYRot() + 180.0F);
        this.roll = -((float)Math.atan2(delta.y, delta.x) - 1f/2f * (float)Math.PI);
        if(this.age == 1)
            this.oRoll = this.roll;
    }

    public static void spawnBurst(ClientLevel level, Vec3 pos, Direction direction) {
//        int size = 5;
//        for(int i = 0; i < size; ++i) {
//            for(int j = 0; j < size; ++j) {
//                for(int k = 0; k < size; ++k) {
//                    double d4 = ((double)i + 0.5D) / (double)size;
//                    double d5 = ((double)j + 0.5D) / (double)size;
//                    double d6 = ((double)k + 0.5D) / (double)size;
//                    level.addParticle(new PortalParticle.PortalParticleData(1, 0, 0, 1),
//                            pos.x + d4, pos.y + d5, pos.z + d6, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D);
//                }
//            }
//        }

        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        Random random = new Random();

        int count = 40;
        for(int i = 0; i < count; i++) {
            Vec3i normal = direction.getNormal();
//            float xd = normal.getX() == 0 ? (float)random.nextInt(50) / 100f : 0;
//            float yd = normal.getY() == 0 ? (float)random.nextInt(50) / 100f : 0;
//            float zd = normal.getZ() == 0 ? (float)random.nextInt(50) / 100f : 0;

            level.addParticle(new PortalParticle.PortalParticleData(0, 0, 1, 1, direction), x, y, z, 0, 0, 0);
        }
    }
    @Override
    protected float getU0() {
        return this.sprite.getU(u * 4);
    }
    @Override
    protected float getV0() {
        return this.sprite.getV(v * 4);
    }
    @Override
    protected float getU1() {
        return this.sprite.getU((u + 1) * 4);
    }
    @Override
    protected float getV1() {
        return this.sprite.getV((v + 1) * 4);
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
        }
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            PortalParticle particle = new PortalParticle(level, x, y, z, xd, yd, zd, (PortalParticleData)type, sprite);
            particle.pickSprite(sprite);
            return particle;
        }
    }
    public static class PortalParticleData extends SimpleParticleType {
        public static final ParticleOptions.IDeserializer<PortalParticleData> DESERIALIZER = new ParticleOptions.IDeserializer<PortalParticleData>() {
            @Override
            public PortalParticleData fromCommand(ParticleType<PortalParticleData> type, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float r = reader.readFloat();
                reader.expect(' ');
                float g = reader.readFloat();
                reader.expect(' ');
                float b = reader.readFloat();
                reader.expect(' ');
                float a = reader.readFloat();
                reader.expect(' ');
                Direction direction = Direction.from3DDataValue(reader.readInt());
                return new PortalParticleData(r, g, b, a, direction);
            }
            @Override
            public PortalParticleData fromNetwork(ParticleType<PortalParticleData> type, FriendlyByteBuf packetBuffer) {
                return new PortalParticleData(packetBuffer.readFloat(), packetBuffer.readFloat(),
                        packetBuffer.readFloat(), packetBuffer.readFloat(), Direction.from3DDataValue(packetBuffer.readInt()));
            }
        };
        private final float r, g, b, a;
        private final Direction direction;
        public PortalParticleData(float r, float g, float b, float a, Direction direction) {
            super(false);
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = Mth.clamp(a, 0.01f, 4.0f);
            this.direction = direction;
        }
        @Override
        public void writeToNetwork(FriendlyByteBuf packetBuffer) {
            packetBuffer.writeFloat(this.r);
            packetBuffer.writeFloat(this.g);
            packetBuffer.writeFloat(this.b);
            packetBuffer.writeFloat(this.a);
            packetBuffer.writeInt(this.direction.get3DDataValue());
        }
        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %d",
                    Registry.PARTICLE_TYPE.getKey(this.getType()), this.r, this.g, this.b, this.a, this.direction.get3DDataValue());
        }
        @Override
        public SimpleParticleType getType() {
            return ParticleInit.PORTAL_PARTICLE.get();
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

        public float getAlpha() {
            return this.a;
        }

        public Direction getDirection() {
            return this.direction;
        }
    }
}