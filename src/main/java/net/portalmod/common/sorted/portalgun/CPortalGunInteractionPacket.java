package net.portalmod.common.sorted.portalgun;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.common.sorted.portal.ITeleportable;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.packet.AbstractPacket;

public class CPortalGunInteractionPacket implements AbstractPacket<CPortalGunInteractionPacket> {
    private PortalGunInteraction type;
    private PortalEnd end;
    private int data;

    public CPortalGunInteractionPacket() {}

    public CPortalGunInteractionPacket(PortalGunInteraction type, PortalEnd end, int data) {
        this.type = type;
        this.end = end;
        this.data = data;
    }

    public static class Builder {
        private final PortalGunInteraction type;
        private PortalEnd end = PortalEnd.NONE;
        private int data = -1;
        
        public Builder(PortalGunInteraction type) {
            this.type = type;
        }
        
        public Builder end(PortalEnd end) {
            this.end = end;
            return this;
        }
        
        public Builder data(int data) {
            this.data = data;
            return this;
        }
        
        public CPortalGunInteractionPacket build() {
            return new CPortalGunInteractionPacket(type, end, data);
        }
    }
    
    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        buffer.writeEnum(end);
        buffer.writeInt(data);
    }

    @Override
    public CPortalGunInteractionPacket decode(PacketBuffer buffer) {
        return new CPortalGunInteractionPacket(
                buffer.readEnum(PortalGunInteraction.class),
                buffer.readEnum(PortalEnd.class),
                buffer.readInt());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PlayerEntity player = context.get().getSender();
            if(player == null)
                return;

            switch(type) {
                case PICK_CUBE:
                    CriteriaTriggerInit.GRAB_ENTITY.get().trigger((ServerPlayerEntity)player);
                    player.level.getEntity(data).startRiding(player);
                    // todo only temporary fix
                    ((ITeleportable)player.level.getEntity(data)).removeLastUsedPortal();
                    break;

                case DROP_CUBE:
                    PortalGun.dropCube(player, false);
                    break;

                case THROW_CUBE:
                    PortalGun.dropCube(player, true);
                    break;

                case SHOOT_PORTAL:
                    PortalGun.placePortal(player, player.level, end, player.getMainHandItem());
                    break;
            }
        });

        context.get().setPacketHandled(true);
        return true;
    }
}