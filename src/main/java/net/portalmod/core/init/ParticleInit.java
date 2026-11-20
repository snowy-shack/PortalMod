package net.portalmod.core.init;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

import net.fabricmc.api.EnvType;
import net.portalmod.core.registry.RegistryObject;
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

public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, PortalMod.MODID);
    public static final RegistryObject<SimpleParticleType> PORTAL_PARTICLE = PARTICLE_TYPES.register("portal_particle",
            () -> FabricParticleTypes.simple(false));
    public static final RegistryObject<SimpleParticleType> PORTAL_PHOTON = PARTICLE_TYPES.register("portal_photon",
            () -> FabricParticleTypes.simple(false));
    public static final RegistryObject<SimpleParticleType> FIZZLE_GLOW = PARTICLE_TYPES.register("fizzle_glow",
            () -> FabricParticleTypes.simple(false));
    public static final RegistryObject<SimpleParticleType> FIZZLE_FLAKE_FALLING = PARTICLE_TYPES.register("fizzle_flake_falling",
            () -> FabricParticleTypes.simple(false));
    public static final RegistryObject<SimpleParticleType> FIZZLE_FLAKE_LANDING = PARTICLE_TYPES.register("fizzle_flake_landing",
            () -> FabricParticleTypes.simple(false));
    public static final RegistryObject<SimpleParticleType> PORTALGUN_SPARK = PARTICLE_TYPES.register("portalgun_spark",
            () -> FabricParticleTypes.simple(false));
    public static final RegistryObject<SimpleParticleType> TURRET_SPARK = PARTICLE_TYPES.register("turret_spark",
            () -> FabricParticleTypes.simple(false));

    // Backport of the small flame for candles, this should get replaced with the vanilla one after porting to a newer version
    public static final RegistryObject<SimpleParticleType> SMALL_FLAME = PARTICLE_TYPES.register("small_flame",
            () -> FabricParticleTypes.simple(false));

    private ParticleInit() {}

    /** Client only: called from the client entrypoint. */
    public static void registerParticleFactories() {
        ParticleFactoryRegistry particleEngine = ParticleFactoryRegistry.getInstance();
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