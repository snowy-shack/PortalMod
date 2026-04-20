package net.portalmod.core.init;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.packet.SUpdateBooleanGameRulePacket;
import net.portalmod.mixins.accessors.BooleanValueAccessor;

import java.util.HashMap;
import java.util.Map;

public class GameRuleInit {
    private GameRuleInit() {}

    public static final Map<String, GameRules.RuleKey<?>> REGISTRY = new HashMap<>();

    public static GameRules.RuleKey<GameRules.BooleanValue> PORTAL_SLOWSHOT;
    public static GameRules.RuleKey<GameRules.BooleanValue> USE_PORTALABLE_BLACKLIST;
    public static GameRules.RuleKey<GameRules.BooleanValue> ALLOW_PORTAL_OVERWRITE;

    public static void registerAll() {
        PORTAL_SLOWSHOT = registerBoolean("portalSlowShot", GameRules.Category.PLAYER, false);
        USE_PORTALABLE_BLACKLIST = registerBoolean("usePortalableBlacklist", GameRules.Category.PLAYER, false);
        // Server-only: the client does not need to know this value. The server consults it at
        // portal-placement time and either evicts the foreign portal in the way or rejects the
        // shot; no client-side behavior (rendering, UI, prediction) depends on it.
        ALLOW_PORTAL_OVERWRITE = registerServerBoolean("allowPortalOverwrite", GameRules.Category.PLAYER, true);
    }

    private static <T extends GameRules.RuleValue<T>> GameRules.RuleKey<T> register(String name, GameRules.Category category, GameRules.RuleType<T> rule) {
        GameRules.RuleKey<T> key = GameRules.register(name, category, rule);
        REGISTRY.put(name, key);
        return key;
    }

    private static GameRules.RuleKey<GameRules.BooleanValue> registerBoolean(String name, GameRules.Category category, boolean defaultValue) {
        return register(name, category, BooleanValueAccessor.pmCreate(defaultValue, (server, value) ->
                PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SUpdateBooleanGameRulePacket(name, value.get()))));
    }

    private static GameRules.RuleKey<GameRules.BooleanValue> registerServerBoolean(String name, GameRules.Category category, boolean defaultValue) {
        return register(name, category, BooleanValueAccessor.pmCreate(defaultValue, (server, value) -> {}));
    }

    @SuppressWarnings("unchecked")
    public static <T extends GameRules.RuleValue<T>> GameRules.RuleKey<T> getRule(String name) {
        return (GameRules.RuleKey<T>)REGISTRY.get(name);
    }

    public static void sendBooleanRule(ServerPlayerEntity player, GameRules.RuleKey<GameRules.BooleanValue> key) {
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new SUpdateBooleanGameRulePacket(key.getId(), player.level.getGameRules().getBoolean(key)));
    }
}