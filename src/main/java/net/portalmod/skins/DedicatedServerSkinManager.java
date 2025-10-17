package net.portalmod.skins;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DedicatedServerSkinManager extends WorldSavedData {
    static String DATA_NAME = "portalgun_skins";

    public final HashMap<UUID, List<String>> playerSkins = new HashMap<>();
    public final List<PortalGunSkin> allSkins = new ArrayList<>();

    public boolean loaded = false;

    public DedicatedServerSkinManager(String name) {
        super(name);
    }

    public CompletableFuture<Void> savePlayerSkins(ServerPlayerEntity player) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<String> skins = APIWrapper.getUserData(player.getStringUUID()).getSecond();
                playerSkins.put(player.getUUID(), skins);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isAllowedSkin(ServerPlayerEntity player, String skin) {
        return playerSkins.get(player.getUUID()).contains(skin); // TODO refetch
    }

    public void init() {
        if (!allSkins.isEmpty()) return;

        asyncLoadSkins().thenRun(() -> {
            System.out.println("Loaded skins");
        });
    }

    public CompletableFuture<Void> asyncLoadSkins() {
        return CompletableFuture.runAsync(() -> {
            System.out.println("server innitting");
        });
    }

    @Override
    public void load(CompoundNBT nbt) {
        // --- Load all available skins ---
        allSkins.clear();
        ListNBT allSkinsList = nbt.getList("AvailablePGSkins", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allSkinsList.size(); i++) {
            CompoundNBT skinTag = allSkinsList.getCompound(i);
            allSkins.add(PortalGunSkin.fromNBT(skinTag));
        }

        // --- Load player skin ownership ---
        playerSkins.clear();
        ListNBT playersList = nbt.getList("PlayerUnlockedPGSkins", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < playersList.size(); i++) {
            CompoundNBT playerEntry = playersList.getCompound(i);

            ListNBT skinIdList = playerEntry.getList("Skins", Constants.NBT.TAG_STRING);
            List<String> skins = new ArrayList<>();

            for (int j = 0; j < skinIdList.size(); j++) {
                skins.add(skinIdList.getString(j));
            }

            playerSkins.put(playerEntry.getUUID("UUID"), skins);
        }

        loaded = true;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        // --- Save all available skins ---
        ListNBT allSkinsList = new ListNBT();
        for (PortalGunSkin skin : allSkins)
            allSkinsList.add(skin.toNBT());

        nbt.put("AvailablePGSkins", allSkinsList);

        // --- Save player unlocked skins ---
        ListNBT playersList = new ListNBT();
        for (Map.Entry<UUID, List<String>> entry : playerSkins.entrySet()) {
            CompoundNBT playerEntry = new CompoundNBT();
            playerEntry.putUUID("UUID", entry.getKey());

            ListNBT skinIdList = new ListNBT();
            for (String id : entry.getValue()) {
                skinIdList.add(StringNBT.valueOf(id));
            }

            playerEntry.put("Skins", skinIdList);
            playersList.add(playerEntry);
        }

        nbt.put("PlayerUnlockedPGSkins", playersList);
        return nbt;
    }

    public static DedicatedServerSkinManager get(ServerWorld world) {
        return world.getDataStorage().computeIfAbsent(() -> new DedicatedServerSkinManager(DATA_NAME), DATA_NAME);
    }
}
