package net.portalmod.skins;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.common.sorted.portalgun.PortalGunISTER;
import net.portalmod.common.sorted.portalgun.api.SkinLoader;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.ModUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClientSkinConfig {
    private static final Gson GSON = new Gson();
    public static Colour tintColor = Colour.WHITE;

    private static boolean initialized = false;
    public static void init() {
        // Already initialised
        if (!PortalModOptionsScreen.AVAILABLE_SKINS.get().isEmpty()) return;

        asyncLoad().thenRun(() -> {
            System.out.println("Loaded skins");
        });
    }

    /**
     * This fetches what skins exist, and sets what skins are unlocked.
     */
    public static CompletableFuture<Void> asyncLoad() {
        return CompletableFuture.runAsync(() -> {
            try {
                List<PortalGunSkin> allSkins = APIWrapper.getAllSkins();

                String playerUUID = Minecraft.getInstance().getUser().getUuid();

                Pair<Integer, List<String>> userData = APIWrapper.getUserData(playerUUID);
                List<String> skinNames = userData.getSecond();

                for (PortalGunSkin skin : allSkins) {
                    skin.unlocked = skinNames.contains(skin.skin_id) || Objects.equals(skin.skin_id, "default");
                }

                setSkins(allSkins);
                if (Objects.equals(PortalModOptionsScreen.SKIN_COL.get(), "") && userData.getFirst() != 0)
                    setColor(userData.getFirst());

                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Store the list of available skins
     */
    public static void setSkins(List<PortalGunSkin> skins) {
        try {
            String json = GSON.toJson(skins);
            String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            PortalModOptionsScreen.AVAILABLE_SKINS.set(encoded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the list of available skins, this should be used for the config screen
     */
    public static List<PortalGunSkin> getSkins() {
        try {
            String encoded = PortalModOptionsScreen.AVAILABLE_SKINS.get();
            if (encoded == null || encoded.isEmpty())
                return Collections.emptyList();

            String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            Type type = new TypeToken<List<PortalGunSkin>>(){}.getType();
            return GSON.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Set the color of the Tintable Portal Gun skin
     */
    public static void setColor(int color) {
        PortalModOptionsScreen.SKIN_COL.set(String.format("%06X", color & 0xFFFFFF));
    }

    /**
     * Load in the current set skin
     */
    public static CompletableFuture<Void> loadCurrentSkin() {
        return CompletableFuture.runAsync(() -> {
            ModUtil.sendChatSinglePlayer("Loading '" + PortalModOptionsScreen.SET_SKIN.get() + "'...");
            if (isDefaultSkin()) return; // No need to load

            String skin_id = PortalModOptionsScreen.SET_SKIN.get();
            PortalGunISTER.SKIN_TEXTURE = SkinLoader.loadSkin(Objects.equals(skin_id, "") ? "default" : skin_id);

            tintColor = getTintColor();
        });
    }

    public static Colour getTintColor() {
        if (!PortalModOptionsScreen.SET_SKIN.get().equals("tintable")) return Colour.WHITE;

        return Colour.fromHex(PortalModOptionsScreen.SKIN_COL.get());
    }

    public static boolean isDefaultSkin() {
        return PortalModOptionsScreen.SET_SKIN.get().equals("default");
    }
}
