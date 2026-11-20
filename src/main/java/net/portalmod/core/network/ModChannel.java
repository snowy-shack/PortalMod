package net.portalmod.core.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.portalmod.core.packet.AbstractPacket;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * The mod's packet channel. Each packet class gets its own Fabric channel id
 * ({@code portalmod:<classname>}), registered with Fabric's networking API.
 */
public final class ModChannel {
    private final String namespace;
    final Map<Class<?>, ResourceLocation> ids = new HashMap<>();
    final Map<ResourceLocation, Function<FriendlyByteBuf, AbstractPacket<?>>> clientBound = new HashMap<>();

    public ModChannel(String namespace) {
        this.namespace = namespace;
    }

    public <T extends AbstractPacket<T>> void register(T template, NetworkDirection direction) {
        ResourceLocation id = new ResourceLocation(namespace, template.getClass().getSimpleName().toLowerCase(Locale.ROOT));
        ids.put(template.getClass(), id);
        if (direction == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buf, sender) -> {
                T msg = template.decode(buf);
                PacketContext ctx = new PacketContext(server, NetworkDirection.PLAY_TO_SERVER, player);
                msg.handle(() -> ctx);
            });
        } else {
            // Client receivers are registered from the client entrypoint (see ModChannelClient).
            clientBound.put(id, template::decode);
        }
    }

    ResourceLocation idOf(AbstractPacket<?> msg) {
        ResourceLocation id = ids.get(msg.getClass());
        if (id == null)
            throw new IllegalStateException("Unregistered packet " + msg.getClass().getName());
        return id;
    }

    static FriendlyByteBuf encode(AbstractPacket<?> msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        msg.encode(buf);
        return buf;
    }

    public Packet<ClientGamePacketListener> toVanillaPacket(AbstractPacket<?> msg, NetworkDirection direction) {
        return ServerPlayNetworking.createS2CPacket(idOf(msg), encode(msg));
    }

    public void send(PacketTarget target, AbstractPacket<?> msg) {
        target.send(toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    /** Client side only. */
    public void sendToServer(AbstractPacket<?> msg) {
        ModChannelClient.sendToServer(this, msg);
    }
}
