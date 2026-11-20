package net.portalmod.common.sorted.portal;

import net.minecraft.core.registries.Registries;
import com.mojang.datafixers.util.Pair;
import net.portalmod.core.network.PacketDistributor;
import net.portalmod.PortalMod;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.math.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class PortalManager extends SavedData {
    private static PortalManager instance;
    public static final String PATH = PortalMod.MODID + "_portals";
    private final Map<UUID, PortalPair> portalMap = new HashMap<>();
    private final Map<Pair<UUID, PortalEnd>, PortalPlacementInfo> scheduledPlacements = new HashMap<>();
    private final HashMap<ResourceKey<Level>, HashMap<ChunkPos, List<PortalEntity>>> portalsPerChunk = new HashMap<>();
    private final Deque<PortalEntity> pendingRemovals = new ArrayDeque<>();
    public boolean unloadingChunk = false;

    private PortalManager() {}

    /** Reads the saved portals into the singleton (1.17+ SavedData loads through a factory). */
    public static PortalManager load(CompoundTag nbt) {
        PortalManager manager = getInstance();
        manager.loadFrom(nbt);
        return manager;
    }

    public static PortalManager getInstance() {
        if(instance == null)
            instance = new PortalManager();
        return instance;
    }

    public void tick() {
        for(PortalEntity portal : this.pendingRemovals) {
            portal.discard();
        }
        this.pendingRemovals.clear();

        List<Pair<UUID, PortalEnd>> completedPlacements = new ArrayList<>();
        this.scheduledPlacements.forEach((key, info) -> {
            if(--info.tickCountdown <= 0) {
                PortalEntity portal = PortalPlacer.placePortal(info.level, info.end, info.hue, info.gunUUID, info.position, info.face, info.upDirection, info.override, info.overwriteForeignPortals, info.lookingDirections, info.player);
                info.onPlace.accept(portal);

                completedPlacements.add(key);
            }
        });
        completedPlacements.forEach(this.scheduledPlacements::remove);
    }

    public void scheduleRemoval(PortalEntity portal) {
        this.pendingRemovals.add(portal);
    }

    public void schedulePlacement(Level level, PortalEnd end, String hue, UUID gunUUID, Vec3 position, Direction face, Direction upDirection, boolean override, @Nullable Direction[] lookingDirections, @Nullable ServerPlayer player, long tickCountdown, Consumer<PortalEntity> onPlace) {
        schedulePlacement(level, end, hue, gunUUID, position, face, upDirection, override, false, lookingDirections, player, tickCountdown, onPlace);
    }

    public void schedulePlacement(Level level, PortalEnd end, String hue, UUID gunUUID, Vec3 position, Direction face, Direction upDirection, boolean override, boolean overwriteForeignPortals, @Nullable Direction[] lookingDirections, @Nullable ServerPlayer player, long tickCountdown, Consumer<PortalEntity> onPlace) {
        Pair<UUID, PortalEnd> key = new Pair<>(gunUUID, end);
        this.scheduledPlacements.put(key, new PortalPlacementInfo(level, end, hue, gunUUID, position, face, upDirection, override, overwriteForeignPortals, lookingDirections, player, tickCountdown, onPlace));
    }

    public void clearScheduledPlacements(UUID gunUUID) {
        List<Pair<UUID, PortalEnd>> removedScheduledPlacements = this.scheduledPlacements.keySet().stream()
                .filter(key -> key.getFirst().equals(gunUUID)).collect(Collectors.toList());
        removedScheduledPlacements.forEach(this.scheduledPlacements::remove);
    }

    private void loadFrom(CompoundTag nbt) {
        for(String key : nbt.getAllKeys()) {
            CompoundTag pair = nbt.getCompound(key);
            PortalPair portalPair = new PortalPair();
            if(pair.contains("primary")) {
                CompoundTag primary = pair.getCompound("primary");
                ResourceLocation rl = new ResourceLocation(primary.getString("level"));
                ResourceKey<Level> rk = ResourceKey.create(Registries.DIMENSION, rl);
                Level level = PortalMod.getServer().getLevel(rk);

                PortalEntity blue = new PortalEntity(level);
                blue.load(primary);
                portalPair.set(PortalEnd.PRIMARY, blue);

                ChunkPos chunkPos = new ChunkPos(Mth.floor(blue.getX()) >> 4, Mth.floor(blue.getZ()) >> 4);
                HashMap<ChunkPos, List<PortalEntity>> chunks = portalsPerChunk.getOrDefault(rk, new HashMap<>());
                List<PortalEntity> portals = chunks.getOrDefault(chunkPos, new ArrayList<>());
                portals.add(blue);
                chunks.put(chunkPos, portals);
                portalsPerChunk.put(rk, chunks);
            }
            if(pair.contains("secondary")) {
                CompoundTag secondary = pair.getCompound("secondary");
                ResourceLocation rl = new ResourceLocation(secondary.getString("level"));
                ResourceKey<Level> rk = ResourceKey.create(Registries.DIMENSION, rl);
                Level level = PortalMod.getServer().getLevel(rk);

                PortalEntity orange = new PortalEntity(level);
                orange.load(secondary);
                portalPair.set(PortalEnd.SECONDARY, orange);

                ChunkPos chunkPos = new ChunkPos(Mth.floor(orange.getX()) >> 4, Mth.floor(orange.getZ()) >> 4);
                HashMap<ChunkPos, List<PortalEntity>> chunks = portalsPerChunk.getOrDefault(rk, new HashMap<>());
                List<PortalEntity> portals = chunks.getOrDefault(chunkPos, new ArrayList<>());
                portals.add(orange);
                chunks.put(chunkPos, portals);
                portalsPerChunk.put(rk, chunks);
            }
            portalMap.put(UUID.fromString(key), portalPair);
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        portalMap.forEach((uuid, pair) -> {
            CompoundTag pairNbt = new CompoundTag();
            if(pair.has(PortalEnd.PRIMARY)) {
                CompoundTag blueNbt = new CompoundTag();
                pair.get(PortalEnd.PRIMARY).saveGlobal(blueNbt);
                blueNbt.putString("level", pair.get(PortalEnd.PRIMARY).level().dimension().location().toString());
                pairNbt.put("primary", blueNbt);
            }
            if(pair.has(PortalEnd.SECONDARY)) {
                CompoundTag orangeNbt = new CompoundTag();
                pair.get(PortalEnd.SECONDARY).saveGlobal(orangeNbt);
                orangeNbt.putString("level", pair.get(PortalEnd.SECONDARY).level().dimension().location().toString());
                pairNbt.put("secondary", orangeNbt);
            }
            nbt.put(uuid.toString(), pairNbt);
        });
        return nbt;
    }

    public void clear() {
        portalMap.clear();
        portalsPerChunk.clear();
    }

    private static Level getOverworld() {
        return PortalMod.getServer().getLevel(Level.OVERWORLD);
    }

    private static void getStorageAndSetDirty() {
        ((ServerLevel)getOverworld()).getDataStorage().computeIfAbsent(PortalManager::load, PortalManager::getInstance, PATH).setDirty();
    }

    public HashMap<ResourceKey<Level>, HashMap<ChunkPos, List<PortalEntity>>> getPortalsPerChunk() {
        return portalsPerChunk;
    }

    public void put(UUID gunUUID, PortalEnd end, PortalEntity portal) {
        PortalPair pair = portalMap.getOrDefault(gunUUID, new PortalPair());

        if(!pair.areInSameDimension(PortalEnd.PRIMARY, portal)) {
            PortalEntity primary = pair.get(PortalEnd.PRIMARY);
            primary.discard();
        }

        if(!pair.areInSameDimension(PortalEnd.SECONDARY, portal)) {
            PortalEntity secondary = pair.get(PortalEnd.SECONDARY);
            secondary.discard();
        }

        pair.computeIfPresent(end, PortalEntity::onReplaced);
        pair.set(end, portal);
        portalMap.put(gunUUID, pair);

        ChunkPos chunkPos = new ChunkPos(Mth.floor(portal.getX()) >> 4, Mth.floor(portal.getZ()) >> 4);
        HashMap<ChunkPos, List<PortalEntity>> chunks = portalsPerChunk.getOrDefault(portal.level().dimension(), new HashMap<>());
        List<PortalEntity> portals = chunks.getOrDefault(chunkPos, new ArrayList<>());
        portals.add(portal);
        chunks.put(chunkPos, portals);
        portalsPerChunk.put(portal.level().dimension(), chunks);

        PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SPortalPairPacket(gunUUID, new PartialPortalPair(pair)));

        getStorageAndSetDirty();
    }

    public void remove(UUID gunUUID, PortalEntity portal) {
        portalMap.computeIfPresent(gunUUID, (uuid, pair) -> {
            pair.remove(portal);

            ResourceKey<Level> dimension = portal.level().dimension();
            ChunkPos chunkPos = new ChunkPos(Mth.floor(portal.getX()) >> 4, Mth.floor(portal.getZ()) >> 4);
            if(portalsPerChunk.containsKey(dimension)) {
                if(portalsPerChunk.get(dimension).containsKey(chunkPos)) {
                    List<PortalEntity> portals = portalsPerChunk.get(dimension).get(chunkPos);
                    portals.remove(portal);
                    if (portals.isEmpty())
                        portalsPerChunk.get(dimension).remove(chunkPos);
                }
            }

            getStorageAndSetDirty();
            return !pair.isEmpty() ? pair : null;
        });

        PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new SPortalPairPacket(gunUUID, new PartialPortalPair(portalMap.getOrDefault(gunUUID, new PortalPair()))));
    }

    public boolean has(UUID gunUUID, PortalEnd end) {
        return portalMap.containsKey(gunUUID) && portalMap.get(gunUUID).has(end);
    }

    @Nullable
    public PortalEntity get(UUID gunUUID, PortalEnd end) {
        return portalMap.containsKey(gunUUID) ? portalMap.get(gunUUID).get(end) : null;
    }

    @Nullable
    public PortalPair getPair(UUID gunUUID) {
        return portalMap.getOrDefault(gunUUID, null);
    }

    public Map<UUID, PortalPair> getPortalMap() {
        return portalMap;
    }

    private static class PortalPlacementInfo {
        public final Level level;
        public final PortalEnd end;
        public final String hue;
        public final UUID gunUUID;
        public final Vec3 position;
        public final Direction face;
        public final Direction upDirection;
        public final boolean override;
        public final boolean overwriteForeignPortals;
        public final @Nullable Direction[] lookingDirections;
        public final @Nullable ServerPlayer player;
        public long tickCountdown;
        public Consumer<PortalEntity> onPlace;

        public PortalPlacementInfo(Level level, PortalEnd end, String hue, UUID gunUUID, Vec3 position, Direction face, Direction upDirection, boolean override, boolean overwriteForeignPortals, @Nullable Direction[] lookingDirections, @Nullable ServerPlayer player, long tickCountdown, Consumer<PortalEntity> onPlace) {
            this.level = level;
            this.end = end;
            this.hue = hue;
            this.gunUUID = gunUUID;
            this.position = position;
            this.face = face;
            this.upDirection = upDirection;
            this.override = override;
            this.overwriteForeignPortals = overwriteForeignPortals;
            this.lookingDirections = lookingDirections;
            this.player = player;
            this.tickCountdown = tickCountdown;
            this.onPlace = onPlace;
        }
    }
}