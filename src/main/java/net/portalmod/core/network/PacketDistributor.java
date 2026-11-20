package net.portalmod.core.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.portalmod.PortalMod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Picks which players receive a server-to-client packet.
 * Same shape as Forge's PacketDistributor, built on Fabric's {@link PlayerLookup}.
 */
public final class PacketDistributor<T> {
    public static final PacketDistributor<Void> ALL =
            new PacketDistributor<>(v -> server() == null ? Collections.emptyList() : PlayerLookup.all(server()));
    public static final PacketDistributor<ServerPlayer> PLAYER =
            new PacketDistributor<>(Collections::singletonList);
    public static final PacketDistributor<ResourceKey<Level>> DIMENSION = new PacketDistributor<>(key -> {
        MinecraftServer server = server();
        ServerLevel level = server == null ? null : server.getLevel(key);
        return level == null ? Collections.emptyList() : PlayerLookup.world(level);
    });
    public static final PacketDistributor<Entity> TRACKING_ENTITY =
            new PacketDistributor<>(PlayerLookup::tracking);
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(entity -> {
        List<ServerPlayer> list = new ArrayList<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayer self && !list.contains(self))
            list.add(self);
        return list;
    });
    public static final PacketDistributor<LevelChunk> TRACKING_CHUNK =
            new PacketDistributor<>(chunk -> PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()));

    private final Function<T, Collection<ServerPlayer>> resolver;

    private PacketDistributor(Function<T, Collection<ServerPlayer>> resolver) {
        this.resolver = resolver;
    }

    private static MinecraftServer server() {
        return PortalMod.getServer();
    }

    public PacketTarget with(Supplier<T> arg) {
        return new PacketTarget(() -> resolver.apply(arg.get()));
    }

    public PacketTarget noArg() {
        return new PacketTarget(() -> resolver.apply(null));
    }
}
