package net.portalmod.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PortalModConfigManager {
    public static ForgeConfigSpec CONFIG;
    public static ForgeConfigSpec.ConfigValue<Boolean> CROSSHAIR;
    public static ForgeConfigSpec.ConfigValue<Boolean> ADHESION_GEL;
    public static ForgeConfigSpec.ConfigValue<Boolean> TOOLTIPS;
    public static ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_GUN;
    public static ForgeConfigSpec.ConfigValue<Boolean> MENU;
    public static ForgeConfigSpec.ConfigValue<Integer> RECURSION;
    public static ForgeConfigSpec.ConfigValue<Boolean> RENDER_SELF;
    public static ForgeConfigSpec.ConfigValue<Boolean> HIGHLIGHTS;
    public static ForgeConfigSpec.ConfigValue<String>  PORTALGUN_SKIN;
    public static ForgeConfigSpec.ConfigValue<Boolean> HAS_SKINS;
    public static ForgeConfigSpec.ConfigValue<Integer> SKIN_TINT;

    public static void init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CROSSHAIR      = builder.define("crosshair", false);
        ADHESION_GEL   = builder.define("adhesion_gel", false);
        CUSTOM_GUN     = builder.define("custom_gun", true);
        MENU           = builder.define("menu", true);
        TOOLTIPS       = builder.define("tooltips", true);
        RECURSION      = builder.define("recursion", 3);
        RENDER_SELF    = builder.define("render_self", true);
        HIGHLIGHTS     = builder.define("highlights", true);
        PORTALGUN_SKIN = builder.define("portalgun_skin", "default");
        HAS_SKINS      = builder.define("has_skins", false);
        SKIN_TINT      = builder.define("skin_tint", 0);

        CONFIG = builder.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG, "portalmod-client.toml");
    }
}