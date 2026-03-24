package net.portalmod.core.packet;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.util.ModUtil;

import java.util.Deque;
import java.util.function.Supplier;

public class CPlayerPortalTeleportLerpPacket implements AbstractPacket<CPlayerPortalTeleportLerpPacket> {
    private Vector3d oldPosition;

    public CPlayerPortalTeleportLerpPacket() {}

    public CPlayerPortalTeleportLerpPacket(Vector3d oldPosition) {
        this.oldPosition = oldPosition;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeDouble(oldPosition.x);
        buffer.writeDouble(oldPosition.y);
        buffer.writeDouble(oldPosition.z);
    }

    @Override
    public CPlayerPortalTeleportLerpPacket decode(PacketBuffer buffer) {
        Vector3d oldPosition = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new CPlayerPortalTeleportLerpPacket(oldPosition);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sender = context.get().getSender();
            if(sender == null)
                return;

            sender.xo = this.oldPosition.x;
            sender.yo = this.oldPosition.y;
            sender.zo = this.oldPosition.z;

            Deque<Tuple<Vector3d, Vector3d>> lerpPositions = ((ITeleportLerpable)sender).getLerpPositions();
            lerpPositions.add(new Tuple<>(ModUtil.getOldPos(sender), sender.position()));

            while(lerpPositions.size() > 3) {
                lerpPositions.removeFirst();
            }
        });

        context.get().setPacketHandled(true);
        return true;
    }
}