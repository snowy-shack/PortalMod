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
import net.portalmod.common.particles.*;
import net.portalmod.common.sorted.portal.PortalParticle;
import net.portalmod.common.sorted.portal.PortalPhotonParticle;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PortalMod.MODID);
    public static final RegistryObject<BasicParticleType> PORTAL_PARTICLE = PARTICLE_TYPES.register("portal_particle",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> PORTAL_PHOTON = PARTICLE_TYPES.register("portal_photon",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> FIZZLE_GLOW = PARTICLE_TYPES.register("fizzle_glow",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> FIZZLE_FLAKE_FALLING = PARTICLE_TYPES.register("fizzle_flake_falling",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> FIZZLE_FLAKE_LANDING = PARTICLE_TYPES.register("fizzle_flake_landing",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> PORTALGUN_SPARK = PARTICLE_TYPES.register("portalgun_spark",
            () -> new BasicParticleType(false));
    public static final RegistryObject<BasicParticleType> TURRET_SPARK = PARTICLE_TYPES.register("turret_spark",
            () -> new BasicParticleType(false));

    // Backport of the small flame for candles, this should get replaced with the vanilla one after porting to a newer version
    public static final RegistryObject<BasicParticleType> SMALL_FLAME = PARTICLE_TYPES.register("small_flame",
            () -> new BasicParticleType(false));

    private ParticleInit() {}

    @SubscribeEvent
    public static void registerParticleFactory(final ParticleFactoryRegisterEvent event) {
        ParticleManager particleEngine = Minecraft.getInstance().particleEngine;
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