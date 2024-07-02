package net.portalmod.common.sorted.portal;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.portalmod.PortalMod;
import net.portalmod.mixins.accessors.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.*;

public class PortalManager extends WorldSavedData {
    private static final PortalManager INSTANCE = new PortalManager();
    public static final String PATH = PortalMod.MODID + "_portals";
    private static final Map<UUID, PortalPair> PORTAL_MAP = new HashMap<>();
    private static final Map<UUID, PortalPair> CLIENTSIDE_MAP = new HashMap<>();
    private static final Map<ChunkPos, List<PortalEntity>> PORTALS_PER_CHUNK = new HashMap<>();
    private static final Map<ChunkPos, List<PortalEntity>> CLIENTSIDE_CACHE = new HashMap<>();

    public PortalManager(String name) {
        super(name);
    }

    private PortalManager() {
        this(PATH);
    }

    @Override
    public void load(CompoundNBT nbt) {
        World ow = getOverworld();
        for(String key : nbt.getAllKeys()) {
            CompoundNBT pair = nbt.getCompound(key);
            PortalPair portalPair = new PortalPair();
            if(pair.contains("primary")) {
                PortalEntity blue = new PortalEntity(ow);
                blue.load(pair.getCompound("primary"));
                blue.inChunk = true;

                RegistryKey<World> levelKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(pair.getCompound("primary").getString("level")));
                ServerWorld level = ServerLifecycleHooks.getCurrentServer().getLevel(levelKey);
                if(level != null) {
                    ((ServerLevelAccessor)level).pmForceAddPortal(blue);
                    portalPair.set(PortalEnd.PRIMARY, blue);
                    ChunkPos chunkPos = new ChunkPos(MathHelper.floor(blue.getX()) >> 4, MathHelper.floor(blue.getZ()) >> 4);
                    List<PortalEntity> portals = PORTALS_PER_CHUNK.getOrDefault(chunkPos, new ArrayList<>());
                    portals.add(blue);
                    PORTALS_PER_CHUNK.put(chunkPos, portals);
                }
            }
            if(pair.contains("secondary")) {
                PortalEntity orange = new PortalEntity(ow);
                orange.load(pair.getCompound("secondary"));
                orange.inChunk = true;

                RegistryKey<World> levelKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(pair.getCompound("secondary").getString("level")));
                ServerWorld level = ServerLifecycleHooks.getCurrentServer().getLevel(levelKey);
                if(level != null) {
                    ((ServerLevelAccessor)level).pmForceAddPortal(orange);
                    portalPair.set(PortalEnd.SECONDARY, orange);
                    ChunkPos chunkPos = new ChunkPos(MathHelper.floor(orange.getX()) >> 4, MathHelper.floor(orange.getZ()) >> 4);
                    List<PortalEntity> portals = PORTALS_PER_CHUNK.getOrDefault(chunkPos, new ArrayList<>());
                    portals.add(orange);
                    PORTALS_PER_CHUNK.put(chunkPos, portals);
                }
            }
            PORTAL_MAP.put(UUID.fromString(key), portalPair);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        PORTAL_MAP.forEach((uuid, pair) -> {
            CompoundNBT pairNbt = new CompoundNBT();
            if(pair.has(PortalEnd.PRIMARY)) {
                CompoundNBT blueNbt = new CompoundNBT();
                pair.get(PortalEnd.PRIMARY).saveGlobal(blueNbt);
                blueNbt.putString("level", pair.get(PortalEnd.PRIMARY).level.dimension().location().toString());
                pairNbt.put("primary", blueNbt);
            }
            if(pair.has(PortalEnd.SECONDARY)) {
                CompoundNBT orangeNbt = new CompoundNBT();
                pair.get(PortalEnd.SECONDARY).saveGlobal(orangeNbt);
                orangeNbt.putString("level", pair.get(PortalEnd.SECONDARY).level.dimension().location().toString());
                pairNbt.put("secondary", orangeNbt);
            }
            nbt.put(uuid.toString(), pairNbt);
        });
        return nbt;
    }

    public static PortalManager getInstance() {
        return INSTANCE;
    }

    public static void clear() {
        PORTAL_MAP.clear();
        PORTALS_PER_CHUNK.clear();

        CLIENTSIDE_MAP.clear();
        CLIENTSIDE_CACHE.clear();
    }

    private static World getOverworld() {
        return ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
    }

    private static void getStorageAndSetDirty() {
        ((ServerWorld)getOverworld()).getDataStorage().computeIfAbsent(PortalManager::getInstance, PATH).setDirty();
    }

    public static Map<ChunkPos, List<PortalEntity>> getPortalsPerChunk() {
        return PORTALS_PER_CHUNK;
    }

    public static Map<ChunkPos, List<PortalEntity>> getCache() {
        return CLIENTSIDE_CACHE;
    }

    public static void putInCache(PortalEntity portal) {
        ChunkPos chunkPos = new ChunkPos(MathHelper.floor(portal.getX()) >> 4, MathHelper.floor(portal.getZ()) >> 4);
        List<PortalEntity> portals = CLIENTSIDE_CACHE.getOrDefault(chunkPos, new ArrayList<>());
        portals.add(portal);
        CLIENTSIDE_CACHE.put(chunkPos, portals);
    }

    public static void removeFromCache(PortalEntity portal) {
        ChunkPos chunkPos = new ChunkPos(MathHelper.floor(portal.getX()) >> 4, MathHelper.floor(portal.getZ()) >> 4);
        if(CLIENTSIDE_CACHE.containsKey(chunkPos)) {
            List<PortalEntity> portals = CLIENTSIDE_CACHE.get(chunkPos);
            portals.remove(portal);
            if(portals.isEmpty())
                CLIENTSIDE_CACHE.remove(chunkPos);
        }
    }

    public static void putInClientMap(UUID gunUUID, PortalEnd end, PortalEntity portal) {
        PortalPair pair = CLIENTSIDE_MAP.getOrDefault(gunUUID, new PortalPair());
        pair.set(end, portal);
        CLIENTSIDE_MAP.put(gunUUID, pair);
    }

    public static void removeFromClientMap(UUID gunUUID, PortalEntity portal) {
        CLIENTSIDE_MAP.computeIfPresent(gunUUID, (uuid, pair) -> {
            pair.remove(portal);
            return !pair.isEmpty() ? pair : null;
        });
    }

    public static void put(UUID gunUUID, PortalEnd end, PortalEntity portal) {
        PortalPair pair = PORTAL_MAP.getOrDefault(gunUUID, new PortalPair());
        pair.computeIfPresent(end, PortalEntity::onReplaced);
        pair.set(end, portal);
        portal.inChunk = true;
        PORTAL_MAP.put(gunUUID, pair);

        ChunkPos chunkPos = new ChunkPos(MathHelper.floor(portal.getX()) >> 4, MathHelper.floor(portal.getZ()) >> 4);
        List<PortalEntity> portals = PORTALS_PER_CHUNK.getOrDefault(chunkPos, new ArrayList<>());
        portals.add(portal);
        PORTALS_PER_CHUNK.put(chunkPos, portals);

        getStorageAndSetDirty();
    }

    public static void remove(UUID gunUUID, PortalEntity portal) {
        PORTAL_MAP.computeIfPresent(gunUUID, (uuid, pair) -> {
            pair.remove(portal);
            portal.inChunk = false;

            ChunkPos chunkPos = new ChunkPos(MathHelper.floor(portal.getX()) >> 4, MathHelper.floor(portal.getZ()) >> 4);
            if(PORTALS_PER_CHUNK.containsKey(chunkPos)) {
                List<PortalEntity> portals = PORTALS_PER_CHUNK.get(chunkPos);
                portals.remove(portal);
                if(portals.isEmpty())
                    PORTALS_PER_CHUNK.remove(chunkPos);
            }

            getStorageAndSetDirty();
            return !pair.isEmpty() ? pair : null;
        });
    }

    public static boolean has(UUID gunUUID, PortalEnd end) {
        if(!PORTAL_MAP.containsKey(gunUUID))
            return false;
        return PORTAL_MAP.get(gunUUID).has(end);
    }

    @Nullable
    public static PortalEntity get(UUID gunUUID, PortalEnd end) {
        if(!PORTAL_MAP.containsKey(gunUUID))
            return null;
        return PORTAL_MAP.get(gunUUID).get(end);
    }

    @Nullable
    public static PortalPair getPair(UUID gunUUID) {
        if(!PORTAL_MAP.containsKey(gunUUID))
            return null;
        return PORTAL_MAP.get(gunUUID);
    }

    public static boolean clientHas(UUID gunUUID, PortalEnd end) {
        if(!CLIENTSIDE_MAP.containsKey(gunUUID))
            return false;
        return CLIENTSIDE_MAP.get(gunUUID).has(end);
    }

    @Nullable
    public static PortalEntity clientGet(UUID gunUUID, PortalEnd end) {
        if(!CLIENTSIDE_MAP.containsKey(gunUUID))
            return null;
        return CLIENTSIDE_MAP.get(gunUUID).get(end);
    }

    @Nullable
    public static PortalPair clientGetPair(UUID gunUUID) {
        if(!CLIENTSIDE_MAP.containsKey(gunUUID))
            return null;
        return CLIENTSIDE_MAP.get(gunUUID);
    }

    public static Map<UUID, PortalPair> getPortalMap() {
        return PORTAL_MAP;
    }
}