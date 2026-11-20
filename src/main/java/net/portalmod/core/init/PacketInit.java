package net.portalmod.core.init;

import net.portalmod.core.network.NetworkDirection;
import net.portalmod.core.network.ModChannel;
import net.portalmod.PortalMod;
import net.portalmod.common.entities.CTestElementHoldingPacket;
import net.portalmod.common.sorted.antline.SAntlineUpdatePacket;
import net.portalmod.common.sorted.faithplate.*;
import net.portalmod.common.sorted.gel.CPropulsionGelBoostTickPacket;
import net.portalmod.common.sorted.gel.CRepulsionGelBouncePacket;
import net.portalmod.common.sorted.portal.CThroughPortalProofPacket;
import net.portalmod.common.sorted.portal.SForgetPortalPacket;
import net.portalmod.common.sorted.portal.SPortalPairPacket;
import net.portalmod.common.sorted.portal.SPortalShotPacket;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.SPortalGunAnimationPacket;
import net.portalmod.common.sorted.portalgun.SPortalGunFailShotPacket;
import net.portalmod.common.sorted.portalgun.skins.CSetPlayerSkinPacket;
import net.portalmod.common.sorted.portalgun.skins.SSetPlayerSkinPacket;
import net.portalmod.common.sorted.radio.SRadioUpdatePacket;
import net.portalmod.common.sorted.trigger.CTriggerAbortConfigPacket;
import net.portalmod.common.sorted.trigger.CTriggerEndConfigPacket;
import net.portalmod.common.sorted.trigger.STriggerStartConfigPacket;
import net.portalmod.core.packet.*;


public class PacketInit {
    public static final ModChannel INSTANCE = new ModChannel(PortalMod.MODID);
    
    private PacketInit() {}
    
    public static void init() {
        register(new SRadioUpdatePacket(),              NetworkDirection.PLAY_TO_CLIENT);
        register(new SAntlineUpdatePacket(),            NetworkDirection.PLAY_TO_CLIENT);
        register(new SFaithPlateLaunchPacket(),         NetworkDirection.PLAY_TO_CLIENT);
        register(new SEntityPortalTeleportLerpPacket(), NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalGunAnimationPacket(),       NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalPairPacket(),               NetworkDirection.PLAY_TO_CLIENT);
        register(new SForgetPortalPacket(),             NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalShotPacket(),               NetworkDirection.PLAY_TO_CLIENT);
        register(new SSetPlayerSkinPacket(),            NetworkDirection.PLAY_TO_CLIENT);
        register(new SPortalGunFailShotPacket(),        NetworkDirection.PLAY_TO_CLIENT);
        register(new SSpawnChamberSignPacket(),         NetworkDirection.PLAY_TO_CLIENT);
        register(new SFaithPlateStartConfigPacket(),    NetworkDirection.PLAY_TO_CLIENT);
        register(new STriggerStartConfigPacket(),       NetworkDirection.PLAY_TO_CLIENT);
        register(new SUpdateBooleanGameRulePacket(),    NetworkDirection.PLAY_TO_CLIENT);

        register(new CFaithPlateUpdatedPacket(),        NetworkDirection.PLAY_TO_SERVER);
        register(new CFaithPlateLaunchPacket(),         NetworkDirection.PLAY_TO_SERVER);
        register(new CPortalGunInteractionPacket(),     NetworkDirection.PLAY_TO_SERVER);
        register(new CPlayerPortalTeleportPacket(),     NetworkDirection.PLAY_TO_SERVER);
        register(new CSetPlayerSkinPacket(),            NetworkDirection.PLAY_TO_SERVER);
        register(new CTestElementHoldingPacket(),       NetworkDirection.PLAY_TO_SERVER);
        register(new CFaithPlateEndConfigPacket(),      NetworkDirection.PLAY_TO_SERVER);
        register(new CTriggerEndConfigPacket(),         NetworkDirection.PLAY_TO_SERVER);
        register(new CTriggerAbortConfigPacket(),       NetworkDirection.PLAY_TO_SERVER);
        register(new CThroughPortalProofPacket(),       NetworkDirection.PLAY_TO_SERVER);
        register(new CPlayerPortalTeleportLerpPacket(), NetworkDirection.PLAY_TO_SERVER);
        register(new CPropulsionGelBoostTickPacket(),   NetworkDirection.PLAY_TO_SERVER);
        register(new CRepulsionGelBouncePacket(),       NetworkDirection.PLAY_TO_SERVER);
    }

    private static <T extends AbstractPacket<T>> void register(T inst, NetworkDirection direction) {
        INSTANCE.register(inst, direction);
    }
}
