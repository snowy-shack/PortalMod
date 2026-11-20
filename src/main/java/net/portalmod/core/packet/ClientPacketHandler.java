package net.portalmod.core.packet;

import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalPhotonParticle;
import net.portalmod.common.sorted.portal.SPortalShotPacket;
import net.portalmod.common.sorted.portalgun.SPortalGunFailShotPacket;
import net.portalmod.common.sorted.sign.ChamberSignEntity;
import net.portalmod.common.sorted.trigger.STriggerStartConfigPacket;
import net.portalmod.common.sorted.trigger.TriggerSelectionClient;
import net.portalmod.common.sorted.trigger.TriggerTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

public class ClientPacketHandler {
    public static void handleSPortalShotPacket(SPortalShotPacket packet) {
        Level level = Minecraft.getInstance().level;
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
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        ChamberSignEntity entity = new ChamberSignEntity(level, packet.pos, packet.direction, packet.verticallyAligned);
        entity.setId(packet.id);
        entity.setUUID(packet.uuid);

        level.putNonPlayerEntity(entity.getId(), entity);
    }

    public static void handleSTriggerStartConfigPacket(STriggerStartConfigPacket packet) {
        Level level = Minecraft.getInstance().level;
        if(level == null)
            return;

        BlockEntity be = level.getBlockEntity(packet.pos);
        if(be instanceof TriggerTileEntity)
            TriggerSelectionClient.startSelecting(((TriggerTileEntity) be));
    }
}