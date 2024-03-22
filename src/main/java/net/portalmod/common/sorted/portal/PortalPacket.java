package net.portalmod.common.sorted.portal;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

public class PortalPacket {
    public static enum Type {
        REPLACE
    }
    
    public static class Client implements AbstractPacket<Client> {
        private final Type type;
        private final int entityId;

        public Client(Type type, int entityId) {
            this.type = type;
            this.entityId = entityId;
        }

        @Override
        public void encode(PacketBuffer buffer) {
            buffer.writeEnum(type)
                    .writeInt(entityId);
        }

        @Override
        public Client decode(PacketBuffer buffer) {
            return new Client(buffer.readEnum(Type.class), buffer.readInt());
        }

        @Override
        public boolean handle(Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if(type == Type.REPLACE)
                        Minecraft.getInstance().level.removeEntity(entityId);
                });
            });

            context.get().setPacketHandled(true);
            return true;
        }
    }
}