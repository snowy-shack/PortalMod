package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.Minecraft;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.init.PacketInit;

public class PortalGunClient {
    protected static void handleLeftClick() {
        if (Minecraft.getInstance().player.hasPassenger(TestElementEntity.class)) {
            PortalGun.dropCube(Minecraft.getInstance().player, true);
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.THROW_ENTITY).build());
        } else PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.PRIMARY).build());
    }

    protected static void handleRightClick() {
        if (!Minecraft.getInstance().player.hasPassenger(TestElementEntity.class))
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.SECONDARY).build());
    }
}