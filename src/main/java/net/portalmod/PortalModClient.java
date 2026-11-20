package net.portalmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.portalmod.client.render.BlockColorHandler;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.event.ClientEvents;
import net.portalmod.core.event.ClientModEvents;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.ParticleInit;
import net.portalmod.core.network.ModChannelClient;

/** Fabric "client" entrypoint (physical client only). */
@Environment(EnvType.CLIENT)
public class PortalModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PortalModConfigManager.init();
        ModChannelClient.registerReceivers(PacketInit.INSTANCE);
        ParticleInit.registerParticleFactories();
        BlockColorHandler.register();
        ClientModEvents.register();
        ClientEvents.register();
    }
}
