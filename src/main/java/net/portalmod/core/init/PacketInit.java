package net.portalmod.core.init;

import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.portalmod.PortalMod;
import net.portalmod.common.entities.CTestElementHoldingPacket;
import net.portalmod.common.sorted.antline.SAntlineUpdatePacket;
import net.portalmod.common.sorted.faithplate.CFaithPlateLaunchPacket;
import net.portalmod.common.sorted.faithplate.CFaithPlateUpdatedPacket;
import net.portalmod.common.sorted.faithplate.SFaithPlateLaunchPacket;
import net.portalmod.common.sorted.portal.SForgetPortalPacket;
import net.portalmod.common.sorted.portal.SPortalPairPacket;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.SPortalGunAnimationPacket;
import net.portalmod.common.sorted.portal.SPortalShotPacket;
import net.portalmod.common.sorted.portalgun.SPortalGunFailShotPacket;
import net.portalmod.common.sorted.radio.SRadioUpdatePacket;
import net.portalmod.core.packet.AbstractPacket;
import net.portalmod.core.packet.CPlayerPortalTeleportPacket;
import net.portalmod.core.packet.SEntityPortalTeleportLerpPacket;
import net.portalmod.core.packet.SEntityPortalTeleportPacketNew;
import net.portalmod.common.sorted.portalgun.skins.CSetPlayerSkinPacket;
import net.portalmod.common.sorted.portalgun.skins.SSetPlayerSkinPacket;

public class PacketInit {
    private static int id = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PortalMod.MODID, "main"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    
    private PacketInit() {}
    
    public static void init() {
        register(new SRadioUpdatePacket(),              NetworkDirection.PLAY_TO_CLIENT);
        register(new SAntlineUpdatePacket(),            NetworkDirection.PLAY_TO_CLIENT);
        register(new SFaithPlateLaunchPacket(),         NetworkDirection.PLAY_TO_CLIENT);
        register(new SEntityPortalTeleportPacketNew(),  NetworkDirection.PLAY_TO_CLIENT);
        register(new SEntityPortalTeleportLerpPacket(), NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalGunAnimationPacket(),       NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalPairPacket(),               NetworkDirection.PLAY_TO_CLIENT);
        register(new SForgetPortalPacket(),             NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalShotPacket(),               NetworkDirection.PLAY_TO_CLIENT);
        register(new SSetPlayerSkinPacket(),            NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalGunFailShotPacket(),        NetworkDirection.PLAY_TO_CLIENT);

        register(new CFaithPlateUpdatedPacket(),        NetworkDirection.PLAY_TO_SERVER);
        register(new CFaithPlateLaunchPacket(),         NetworkDirection.PLAY_TO_SERVER);
        register(new CPortalGunInteractionPacket(),     NetworkDirection.PLAY_TO_SERVER);
        register(new CPlayerPortalTeleportPacket(),     NetworkDirection.PLAY_TO_SERVER);
        register(new CSetPlayerSkinPacket(),            NetworkDirection.PLAY_TO_SERVER);
        register(new CTestElementHoldingPacket(),       NetworkDirection.PLAY_TO_SERVER);

        // TODO use this below too
        
//        INSTANCE.messageBuilder(FaithPlateUpdatePacket.Client.class, ID++, NetworkDirection.PLAY_TO_CLIENT)
//                .encoder(FaithPlateUpdatePacket.Client::encode)
//                .decoder(FaithPlateUpdatePacket.Client::new)
//                .consumer(FaithPlateUpdatePacket.Client::handle)
//                .add();
    }
    
    private static <T extends AbstractPacket<T>> void register(T inst, NetworkDirection direction) {
        INSTANCE.messageBuilder((Class<T>)inst.getClass(), id++, direction)
                .encoder(AbstractPacket::encode)
                .decoder(inst::decode)
                .consumer((SimpleChannel.MessageBuilder.ToBooleanBiFunction<T, Supplier<NetworkEvent.Context>>)
                        AbstractPacket::handle)
                .add();
    }
}