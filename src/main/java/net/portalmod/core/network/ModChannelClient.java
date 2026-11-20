package net.portalmod.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.portalmod.core.packet.AbstractPacket;

/** Client half of {@link ModChannel}; kept separate so servers never load client classes. */
@Environment(EnvType.CLIENT)
public final class ModChannelClient {
    private ModChannelClient() {}

    public static void registerReceivers(ModChannel channel) {
        registerEntitySpawnReceiver();

        channel.clientBound.forEach((id, decoder) ->
                ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, sender) -> {
                    AbstractPacket<?> msg = decoder.apply(buf);
                    PacketContext ctx = new PacketContext(client, NetworkDirection.PLAY_TO_CLIENT, null);
                    msg.handle(() -> ctx);
                }));
    }

    private static void registerEntitySpawnReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(EntitySpawnPackets.ID, (client, handler, buf, sender) -> {
            ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(buf);
            // The buffer is freed after this method returns, so copy the extra data for the main thread.
            FriendlyByteBuf extra = new FriendlyByteBuf(Unpooled.copiedBuffer(buf));
            client.execute(() -> {
                try {
                    ClientLevel level = client.level;
                    if (level == null)
                        return;
                    Entity entity = packet.getType().create(level);
                    if (entity == null)
                        return;
                    entity.recreateFromPacket(packet);
                    if (entity instanceof IEntityAdditionalSpawnData data)
                        data.readSpawnData(extra);
                    level.putNonPlayerEntity(packet.getId(), entity);
                } finally {
                    extra.release();
                }
            });
        });
    }

    static void sendToServer(ModChannel channel, AbstractPacket<?> msg) {
        ClientPlayNetworking.send(channel.idOf(msg), ModChannel.encode(msg));
    }
}
