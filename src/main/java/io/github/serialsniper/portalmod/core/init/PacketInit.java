package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.core.packet.AbstractPacket;
import io.github.serialsniper.portalmod.core.packet.AntlineUpdatePacket;
import io.github.serialsniper.portalmod.core.packet.EntityTeleportClientPacket;
import io.github.serialsniper.portalmod.core.packet.FaithPlateUpdatePacket;
import io.github.serialsniper.portalmod.core.packet.PortalGunInteractionPacket;
import io.github.serialsniper.portalmod.core.packet.RadioUpdateClientPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketInit {
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PortalMod.MODID, "main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void init() {
//        INSTANCE.messageBuilder(RadioStatePacket.Server.class, ID++, NetworkDirection.PLAY_TO_SERVER)
//                .encoder(RadioStatePacket.Server::encode)
//                .decoder(RadioStatePacket.Server::new)
//                .consumer(RadioStatePacket.Server::handle)
//                .add();

        INSTANCE.messageBuilder(RadioUpdateClientPacket.class, ID++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RadioUpdateClientPacket::encode)
                .decoder(RadioUpdateClientPacket::new)
                .consumer(RadioUpdateClientPacket::handle)
                .add();

//        INSTANCE.messageBuilder(AntlineUpdatePacket.Server.class, ID++, NetworkDirection.PLAY_TO_SERVER)
//                .encoder(AntlineUpdatePacket.Server::encode)
//                .decoder(AntlineUpdatePacket.Server::new)
//                .consumer(AntlineUpdatePacket.Server::handle)
//                .add();
//
        INSTANCE.messageBuilder(AntlineUpdatePacket.Client.class, ID++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AntlineUpdatePacket.Client::encode)
                .decoder(AntlineUpdatePacket.Client::new)
                .consumer(AntlineUpdatePacket.Client::handle)
                .add();

        INSTANCE.messageBuilder(FaithPlateUpdatePacket.Server.class, ID++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FaithPlateUpdatePacket.Server::encode)
                .decoder(FaithPlateUpdatePacket.Server::new)
                .consumer(FaithPlateUpdatePacket.Server::handle)
                .add();

        INSTANCE.messageBuilder(PortalGunInteractionPacket.Server.class, ID++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PortalGunInteractionPacket.Server::encode)
                .decoder(PortalGunInteractionPacket.Server::new)
                .consumer(PortalGunInteractionPacket.Server::handle)
                .add();
        
        INSTANCE.messageBuilder(PortalGunInteractionPacket.Server.class, ID++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PortalGunInteractionPacket.Server::encode)
                .decoder(PortalGunInteractionPacket.Server::new)
                .consumer(PortalGunInteractionPacket.Server::handle)
                .add();
        

        INSTANCE.messageBuilder(EntityTeleportClientPacket.class, ID++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EntityTeleportClientPacket::encode)
                .decoder(EntityTeleportClientPacket::new)
                .consumer(EntityTeleportClientPacket::handle)
                .add();
        
//        INSTANCE.messageBuilder(FaithPlateUpdatePacket.Client.class, ID++, NetworkDirection.PLAY_TO_CLIENT)
//                .encoder(FaithPlateUpdatePacket.Client::encode)
//                .decoder(FaithPlateUpdatePacket.Client::new)
//                .consumer(FaithPlateUpdatePacket.Client::handle)
//                .add();
    }
}