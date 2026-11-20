package net.portalmod.core.packet;

import net.portalmod.core.network.PacketContext;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.util.ModUtil;

import java.util.Deque;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

public class CPlayerPortalTeleportLerpPacket implements AbstractPacket<CPlayerPortalTeleportLerpPacket> {
    private Vec3 oldPosition;

    public CPlayerPortalTeleportLerpPacket() {}

    public CPlayerPortalTeleportLerpPacket(Vec3 oldPosition) {
        this.oldPosition = oldPosition;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(oldPosition.x);
        buffer.writeDouble(oldPosition.y);
        buffer.writeDouble(oldPosition.z);
    }

    @Override
    public CPlayerPortalTeleportLerpPacket decode(FriendlyByteBuf buffer) {
        Vec3 oldPosition = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new CPlayerPortalTeleportLerpPacket(oldPosition);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if(sender == null)
                return;

            sender.xo = this.oldPosition.x;
            sender.yo = this.oldPosition.y;
            sender.zo = this.oldPosition.z;

            Deque<Tuple<Vec3, Vec3>> lerpPositions = ((ITeleportLerpable)sender).getLerpPositions();
            lerpPositions.add(new Tuple<>(ModUtil.getOldPos(sender), sender.position()));

            while(lerpPositions.size() > 3) {
                lerpPositions.removeFirst();
            }
        });

        context.get().setPacketHandled(true);
        return true;
    }
}