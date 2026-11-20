package net.portalmod.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.portalmod.core.packet.AbstractPacket;

/** Client half of {@link ModChannel}; kept separate so servers never load client classes. */
@Environment(EnvType.CLIENT)
public final class ModChannelClient {
    private ModChannelClient() {}

    public static void registerReceivers(ModChannel channel) {
        channel.clientBound.forEach((id, decoder) ->
                ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, sender) -> {
                    AbstractPacket<?> msg = decoder.apply(buf);
                    PacketContext ctx = new PacketContext(client, NetworkDirection.PLAY_TO_CLIENT, null);
                    msg.handle(() -> ctx);
                }));
    }

    static void sendToServer(ModChannel channel, AbstractPacket<?> msg) {
        ClientPlayNetworking.send(channel.idOf(msg), ModChannel.encode(msg));
    }
}
