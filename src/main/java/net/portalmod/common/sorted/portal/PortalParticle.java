package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.portalmod.core.init.ParticleInit;

import java.util.Locale;
import java.util.Random;

public class PortalParticle extends SpriteTexturedParticle {
    private final int u, v;
    protected PortalParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, PortalParticleData data, IAnimatedSprite sprite) {
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
        this.alpha = MathHelper.clamp((float)Math.pow((this.lifetime - this.age) / 7f, 2), 0, 1);

        this.oRoll = this.roll;
        Vector3d delta = new Vector3d(this.xd, this.yd, this.zd);

        ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        delta.xRot(camera.getXRot()).yRot(camera.getYRot() + 180.0F);
        this.roll = -((float)Math.atan2(delta.y, delta.x) - 1f/2f * (float)Math.PI);
        if(this.age == 1)
            this.oRoll = this.roll;
    }

    public static void spawnBurst(ClientWorld level, Vector3d pos, Direction direction) {
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
            Vector3i normal = direction.getNormal();
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
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;
        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xd, double yd, double zd) {
            PortalParticle particle = new PortalParticle(level, x, y, z, xd, yd, zd, (PortalParticleData)type, sprite);
            particle.pickSprite(sprite);
            return particle;
        }
    }
    public static class PortalParticleData extends BasicParticleType {
        public static final IParticleData.IDeserializer<PortalParticleData> DESERIALIZER = new IParticleData.IDeserializer<PortalParticleData>() {
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
            public PortalParticleData fromNetwork(ParticleType<PortalParticleData> type, PacketBuffer packetBuffer) {
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
            this.a = MathHelper.clamp(a, 0.01f, 4.0f);
            this.direction = direction;
        }
        @Override
        public void writeToNetwork(PacketBuffer packetBuffer) {
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
        public BasicParticleType getType() {
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