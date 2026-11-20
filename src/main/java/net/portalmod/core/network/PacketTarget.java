package net.portalmod.core.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.Supplier;

/** A resolved set of players to send a packet to. */
public final class PacketTarget {
    private final Supplier<Collection<ServerPlayer>> players;

    PacketTarget(Supplier<Collection<ServerPlayer>> players) {
        this.players = players;
    }

    void send(Packet<?> packet) {
        for (ServerPlayer player : players.get()) {
            player.connection.send(packet);
        }
    }
}
