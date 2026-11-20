package net.portalmod.core.network;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** What a packet handler gets: the thread to run game logic on, and the sender (server side only). */
public final class PacketContext {
    private final Executor executor;
    private final NetworkDirection direction;
    @Nullable
    private final ServerPlayer sender;

    public PacketContext(Executor executor, NetworkDirection direction, @Nullable ServerPlayer sender) {
        this.executor = executor;
        this.direction = direction;
        this.sender = sender;
    }

    /** Runs the work on the main game thread (packets arrive on the network thread). */
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return CompletableFuture.runAsync(work, executor);
    }

    @Nullable
    public ServerPlayer getSender() {
        return sender;
    }

    public NetworkDirection getDirection() {
        return direction;
    }

    /** Fabric needs no "handled" flag; kept so handlers compile unchanged. */
    public void setPacketHandled(boolean handled) {}
}
