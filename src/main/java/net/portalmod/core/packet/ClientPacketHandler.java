package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalPhotonParticle;
import net.portalmod.common.sorted.portal.SPortalShotPacket;
import net.portalmod.common.sorted.portalgun.SPortalGunFailShotPacket;
import net.portalmod.common.sorted.sign.ChamberSignEntity;
import net.portalmod.common.sorted.trigger.STriggerStartConfigPacket;
import net.portalmod.common.sorted.trigger.TriggerSelectionClient;
import net.portalmod.common.sorted.trigger.TriggerTileEntity;

public class ClientPacketHandler {
    public static void handleSPortalShotPacket(SPortalShotPacket packet) {
        World level = Minecraft.getInstance().level;
        if(level == null)
            return;

        PortalEntity portal = (PortalEntity)level.getEntity(packet.id);
        if(portal != null)
            PortalPhotonParticle.createOpeningParticles(portal);
    }

    public static void handleSPortalGunFailShotPacket(SPortalGunFailShotPacket packet) {
        PortalPhotonParticle.createFailParticles(Minecraft.getInstance().level, packet.position, packet.normal, packet.upVector, packet.dyeColor);
    }

    public static void handleSSpawnChamberSignPacket(SSpawnChamberSignPacket packet) {
        ClientWorld level = Minecraft.getInstance().level;
        if (level == null) return;

        ChamberSignEntity entity = new ChamberSignEntity(level, packet.pos, packet.direction, packet.verticallyAligned);
        entity.setId(packet.id);
        entity.setUUID(packet.uuid);

        level.putNonPlayerEntity(entity.getId(), entity);
    }

    public static void handleSTriggerStartConfigPacket(STriggerStartConfigPacket packet) {
        World level = Minecraft.getInstance().level;
        if(level == null)
            return;

        TileEntity be = level.getBlockEntity(packet.pos);
        if(be instanceof TriggerTileEntity)
            TriggerSelectionClient.startSelecting(((TriggerTileEntity) be));
    }
}