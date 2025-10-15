package net.portalmod.skins;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.portalmod.client.screens.PortalModOptionsScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ClientSkinConfig {
    private static final Gson GSON = new Gson();

    private static boolean initialized = false;
    public static void init() {
        // Already initialised
        if (!PortalModOptionsScreen.AVAILABLE_SKINS.get().isEmpty()) return;

        load().thenRun(() -> {
            System.out.println("Loaded skins");
        });
    }

    public static CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(() -> {
            try {
                List<PortalGunSkin> allSkins = APIWrapper.getAllSkins();

                String playerUUID = Minecraft.getInstance().getUser().getUuid();

                Pair<Integer, List<String>> userData = APIWrapper.getUserData(playerUUID);
                List<String> skinNames = userData.getSecond();

                for (PortalGunSkin skin : allSkins) {
                    skin.unlocked = skinNames.contains(skin.skin_id) || Objects.equals(skin.skin_id, "default");
                }

                if (!allSkins.isEmpty()) setSkins(allSkins);
                setColDefault(userData.getFirst()); // If no value is already set

                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static List<PortalGunSkin> getSkins() {
        List<PortalGunSkin> list = new ArrayList<>();
        for (String json : PortalModOptionsScreen.AVAILABLE_SKINS.get()) {
            try {
                list.add(GSON.fromJson(json, PortalGunSkin.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void setSkins(List<PortalGunSkin> skins) {
        List<String> jsons = new ArrayList<>();
        for (PortalGunSkin s : skins) {
            jsons.add(GSON.toJson(s));
        }
        PortalModOptionsScreen.AVAILABLE_SKINS.set(jsons);
    }

    public static void setColDefault(int color) {
        if (Objects.equals(PortalModOptionsScreen.SKIN_COL.get(), "") && color != 0)
                PortalModOptionsScreen.SKIN_COL.set(Integer.toHexString(color));
    }
}
