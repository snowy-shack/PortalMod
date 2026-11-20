package net.portalmod.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.portalmod.PortalMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Client config, stored as JSON in {@code config/portalmod-client.json}.
 * Replaces Forge's ForgeConfigSpec; entries keep the same names and defaults.
 */
public class PortalModConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<ConfigValue<?>> VALUES = new ArrayList<>();
    private static Path file;

    public static ConfigValue<Boolean> CROSSHAIR;
    public static ConfigValue<Boolean> ADHESION_GEL;
    public static ConfigValue<Boolean> TOOLTIPS;
    public static ConfigValue<Boolean> CUSTOM_GUN;
    public static ConfigValue<Boolean> MENU;
    public static ConfigValue<Integer> RECURSION;
    public static ConfigValue<Boolean> RENDER_SELF;
    public static ConfigValue<Boolean> HIGHLIGHTS;
    public static ConfigValue<Boolean> PORTAL_FUNNELING;
    public static ConfigValue<Boolean> SEPARATE_GUN;
    public static ConfigValue<String>  PORTALGUN_SKIN;
    public static ConfigValue<Boolean> HAS_SKINS;
    public static ConfigValue<Integer> SKIN_TINT;

    public static void init() {
        file = FabricLoader.getInstance().getConfigDir().resolve("portalmod-client.json");

        CROSSHAIR        = define("crosshair", false);
        ADHESION_GEL     = define("adhesion_gel", false);
        CUSTOM_GUN       = define("custom_gun", true);
        MENU             = define("menu", true);
        TOOLTIPS         = define("tooltips", true);
        RECURSION        = define("recursion", 3);
        RENDER_SELF      = define("render_self", true);
        HIGHLIGHTS       = define("highlights", true);
        PORTAL_FUNNELING = define("portal_funneling", true);
        SEPARATE_GUN     = define("separate_gun", true);
        PORTALGUN_SKIN   = define("portalgun_skin", "default");
        HAS_SKINS        = define("has_skins", false);
        SKIN_TINT        = define("skin_tint", 0);

        load();
        save();
    }

    private static <T> ConfigValue<T> define(String key, T defaultValue) {
        ConfigValue<T> value = new ConfigValue<>(key, defaultValue, PortalModConfigManager::save);
        VALUES.add(value);
        return value;
    }

    @SuppressWarnings("unchecked")
    private static void load() {
        if (!Files.exists(file))
            return;
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null)
                return;
            for (ConfigValue<?> v : VALUES) {
                JsonElement e = json.get(v.key);
                if (e == null)
                    continue;
                // Read with the same type as the default; bad values fall back to the default.
                Object parsed = GSON.fromJson(e, v.defaultValue.getClass());
                if (parsed != null)
                    ((ConfigValue<Object>) v).value = parsed;
            }
        } catch (Exception e) {
            PortalMod.LOGGER.warn("Could not read {}, using defaults", file, e);
        }
    }

    private static void save() {
        if (file == null)
            return;
        JsonObject json = new JsonObject();
        for (ConfigValue<?> v : VALUES)
            json.add(v.key, GSON.toJsonTree(v.value));
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            PortalMod.LOGGER.warn("Could not save {}", file, e);
        }
    }
}
