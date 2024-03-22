package net.portalmod.core.init;

import net.minecraft.client.Minecraft;
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
import net.portalmod.common.sorted.portal.PortalParticle;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PortalMod.MODID);
    public static final RegistryObject<BasicParticleType> PORTAL_PARTICLE = PARTICLE_TYPES.register("portal_particle",
            () -> new BasicParticleType(false));

    private ParticleInit() {}

    @SubscribeEvent
    public static void registerParticleFactory(final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ParticleInit.PORTAL_PARTICLE.get(), PortalParticle.Factory::new);
    }
}