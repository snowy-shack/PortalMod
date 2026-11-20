package net.portalmod.core.init;

import net.fabricmc.api.EnvType;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.portalmod.core.registry.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.core.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.particles.*;
import net.portalmod.common.sorted.portal.PortalParticle;
import net.portalmod.common.sorted.portal.PortalPhotonParticle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = EnvType.CLIENT)
public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, PortalMod.MODID);
    public static final RegistryObject<SimpleParticleType> PORTAL_PARTICLE = PARTICLE_TYPES.register("portal_particle",
            () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> PORTAL_PHOTON = PARTICLE_TYPES.register("portal_photon",
            () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FIZZLE_GLOW = PARTICLE_TYPES.register("fizzle_glow",
            () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FIZZLE_FLAKE_FALLING = PARTICLE_TYPES.register("fizzle_flake_falling",
            () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FIZZLE_FLAKE_LANDING = PARTICLE_TYPES.register("fizzle_flake_landing",
            () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> PORTALGUN_SPARK = PARTICLE_TYPES.register("portalgun_spark",
            () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> TURRET_SPARK = PARTICLE_TYPES.register("turret_spark",
            () -> new SimpleParticleType(false));

    // Backport of the small flame for candles, this should get replaced with the vanilla one after porting to a newer version
    public static final RegistryObject<SimpleParticleType> SMALL_FLAME = PARTICLE_TYPES.register("small_flame",
            () -> new SimpleParticleType(false));

    private ParticleInit() {}

    @SubscribeEvent
    public static void registerParticleFactory(final ParticleFactoryRegisterEvent event) {
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(ParticleInit.PORTAL_PARTICLE.get(), PortalParticle.Factory::new);
        particleEngine.register(ParticleInit.PORTAL_PHOTON.get(), PortalPhotonParticle.Factory::new);
        particleEngine.register(ParticleInit.FIZZLE_GLOW.get(), FizzleGlowParticle.Factory::new);
        particleEngine.register(ParticleInit.FIZZLE_FLAKE_FALLING.get(), FizzleFlakeParticle.FallingFactory::new);
        particleEngine.register(ParticleInit.FIZZLE_FLAKE_LANDING.get(), FizzleFlakeParticle.LandingFactory::new);
        particleEngine.register(ParticleInit.PORTALGUN_SPARK.get(), PortalGunSparkParticle.Factory::new);
        particleEngine.register(ParticleInit.TURRET_SPARK.get(), TurretSparkParticle.Factory::new);
        particleEngine.register(ParticleInit.SMALL_FLAME.get(), SmallFlameFactory::new);
    }
}