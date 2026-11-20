package net.portalmod.common.sorted.portal;

import java.util.function.Supplier;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

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
        public void encode(FriendlyByteBuf buffer) {
            buffer.writeEnum(type)
                    .writeInt(entityId);
        }

        @Override
        public Client decode(FriendlyByteBuf buffer) {
            return new Client(buffer.readEnum(Type.class), buffer.readInt());
        }

        @Override
        public boolean handle(Supplier<PacketContext> context) {
            context.get().enqueueWork(() -> {
                ClientOnly.run(() -> () -> {
                    if(type == Type.REPLACE)
                        Minecraft.getInstance().level.removeEntity(entityId);
                });
            });

            context.get().setPacketHandled(true);
            return true;
        }
    }
}