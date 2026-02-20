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
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.portalmod.PortalMod;
import net.portalmod.core.init.PacketInit;

import javax.annotation.Nullable;
import java.util.*;

public class PortalManager extends WorldSavedData {
    private static PortalManager instance;
    public static final String PATH = PortalMod.MODID + "_portals";
    private final Map<UUID, PortalPair> portalMap = new HashMap<>();
    private final HashMap<RegistryKey<World>, HashMap<ChunkPos, List<PortalEntity>>> portalsPerChunk = new HashMap<>();
    private final Deque<PortalEntity> pendingRemovals = new ArrayDeque<>();
    public boolean unloadingChunk = false;

    private PortalManager() {
        super(PATH);
    }

    public static PortalManager getInstance() {
        if(instance == null)
            instance = new PortalManager();
        return instance;
    }

    public void tick() {
        for(PortalEntity portal : this.pendingRemovals) {
            ((ServerWorld)portal.level).removeEntity(portal, false);
        }
        this.pendingRemovals.clear();
    }

    public void scheduleRemoval(PortalEntity portal) {
        this.pendingRemovals.add(portal);
    }

    @Override
    public void load(CompoundNBT nbt) {
        for(String key : nbt.getAllKeys()) {
            CompoundNBT pair = nbt.getCompound(key);
            PortalPair portalPair = new PortalPair();
            if(pair.contains("primary")) {
                CompoundNBT primary = pair.getCompound("primary");
                ResourceLocation rl = new ResourceLocation(primary.getString("level"));
                RegistryKey<World> rk = RegistryKey.create(Registry.DIMENSION_REGISTRY, rl);
                World level = ServerLifecycleHooks.getCurrentServer().getLevel(rk);

                PortalEntity blue = new PortalEntity(level);
                blue.load(primary);
                portalPair.set(PortalEnd.PRIMARY, blue);

                ChunkPos chunkPos = new ChunkPos(MathHelper.floor(blue.getX()) >> 4, MathHelper.floor(blue.getZ()) >> 4);
                HashMap<ChunkPos, List<PortalEntity>> chunks = portalsPerChunk.getOrDefault(rk, new HashMap<>());
                List<PortalEntity> portals = chunks.getOrDefault(chunkPos, new ArrayList<>());
                portals.add(blue);
                chunks.put(chunkPos, portals);
                portalsPerChunk.put(rk, chunks);
            }
            if(pair.contains("secondary")) {
                CompoundNBT secondary = pair.getCompound("secondary");
                ResourceLocation rl = new ResourceLocation(secondary.getString("level"));
                RegistryKey<World> rk = RegistryKey.create(Registry.DIMENSION_REGISTRY, rl);
                World level = ServerLifecycleHooks.getCurrentServer().getLevel(rk);

                PortalEntity orange = new PortalEntity(level);
                orange.load(secondary);
                portalPair.set(PortalEnd.SECONDARY, orange);

                ChunkPos chunkPos = new ChunkPos(MathHelper.floor(orange.getX()) >> 4, MathHelper.floor(orange.getZ()) >> 4);
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
    public CompoundNBT save(CompoundNBT nbt) {
        portalMap.forEach((uuid, pair) -> {
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

    public void clear() {
        portalMap.clear();
        portalsPerChunk.clear();
    }

    private static World getOverworld() {
        return ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
    }

    private static void getStorageAndSetDirty() {
        ((ServerWorld)getOverworld()).getDataStorage().computeIfAbsent(PortalManager::getInstance, PATH).setDirty();
    }

    public HashMap<RegistryKey<World>, HashMap<ChunkPos, List<PortalEntity>>> getPortalsPerChunk() {
        return portalsPerChunk;
    }

    public void put(UUID gunUUID, PortalEnd end, PortalEntity portal) {
        PortalPair pair = portalMap.getOrDefault(gunUUID, new PortalPair());

        if(!pair.areInSameDimension(PortalEnd.PRIMARY, portal)) {
            PortalEntity primary = pair.get(PortalEnd.PRIMARY);
            ((ServerWorld)primary.level).removeEntity(primary, false);
        }

        if(!pair.areInSameDimension(PortalEnd.SECONDARY, portal)) {
            PortalEntity secondary = pair.get(PortalEnd.SECONDARY);
            ((ServerWorld)secondary.level).removeEntity(secondary, false);
        }

        pair.computeIfPresent(end, PortalEntity::onReplaced);
        pair.set(end, portal);
        portal.inChunk = true;
        portalMap.put(gunUUID, pair);

        ChunkPos chunkPos = new ChunkPos(MathHelper.floor(portal.getX()) >> 4, MathHelper.floor(portal.getZ()) >> 4);
        HashMap<ChunkPos, List<PortalEntity>> chunks = portalsPerChunk.getOrDefault(portal.level.dimension(), new HashMap<>());
        List<PortalEntity> portals = chunks.getOrDefault(chunkPos, new ArrayList<>());
        portals.add(portal);
        chunks.put(chunkPos, portals);
        portalsPerChunk.put(portal.level.dimension(), chunks);

        PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SPortalPairPacket(gunUUID, new PartialPortalPair(pair)));

        getStorageAndSetDirty();
    }

    public void remove(UUID gunUUID, PortalEntity portal) {
        portalMap.computeIfPresent(gunUUID, (uuid, pair) -> {
            pair.remove(portal);
            portal.inChunk = false;

            RegistryKey<World> dimension = portal.level.dimension();
            ChunkPos chunkPos = new ChunkPos(MathHelper.floor(portal.getX()) >> 4, MathHelper.floor(portal.getZ()) >> 4);
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
}