package net.portalmod.core.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.particles.FizzleFlakeParticle;
import net.portalmod.common.particles.FizzleGlowParticle;
import net.portalmod.common.particles.PortalGunSparkParticle;
import net.portalmod.common.sorted.portal.PortalParticle;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PortalMod.MODID);
    public static final RegistryObject<BasicParticleType> PORTAL_PARTICLE = PARTICLE_TYPES.register("portal_particle",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> FIZZLE_GLOW = PARTICLE_TYPES.register("fizzle_glow",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> FIZZLE_FLAKE_FALLING = PARTICLE_TYPES.register("fizzle_flake_falling",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> FIZZLE_FLAKE_LANDING = PARTICLE_TYPES.register("fizzle_flake_landing",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> PORTALGUN_SPARK = PARTICLE_TYPES.register("portalgun_spark",
            () -> new BasicParticleType(false));

//    public static final RegistryObject<BasicParticleType> SMALL_FLAME = PARTICLE_TYPES.register("small_flame",
//            () -> new BasicParticleType(false));

    private ParticleInit() {}

    @SubscribeEvent
    public static void registerParticleFactory(final ParticleFactoryRegisterEvent event) {
        ParticleManager particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(ParticleInit.PORTAL_PARTICLE.get(), PortalParticle.Factory::new);
        particleEngine.register(ParticleInit.FIZZLE_GLOW.get(), FizzleGlowParticle.Factory::new);
        particleEngine.register(ParticleInit.FIZZLE_FLAKE_FALLING.get(), FizzleFlakeParticle.FallingFactory::new);
        particleEngine.register(ParticleInit.FIZZLE_FLAKE_LANDING.get(), FizzleFlakeParticle.LandingFactory::new);
        particleEngine.register(ParticleInit.PORTALGUN_SPARK.get(), PortalGunSparkParticle.Factory::new);
    }

//    public static class SmallFlameParticle implements IParticleFactory<BasicParticleType> {
//        private final IAnimatedSprite sprite;
//
//        public SmallFlameParticle(IAnimatedSprite spriteSet) {
//            this.sprite = spriteSet;
//        }
//
//        @Nullable
//        @Override
//        public Particle createParticle(BasicParticleType basicParticleType, ClientWorld clientLevel, double v, double v1, double v2, double v3, double v4, double v5) {
//            FlameParticle flameParticle = new FlameParticle(clientLevel, v, v1, v2, v3, v4, v5);
//            flameParticle.pickSprite(this.sprite);
//            flameParticle.scale(0.5F);
//            return flameParticle;
//        }
//    }
}