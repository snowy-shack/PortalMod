package net.portalmod.core.init;

import net.portalmod.core.network.PacketDistributor;
import net.portalmod.core.packet.SUpdateBooleanGameRulePacket;
import net.portalmod.mixins.accessors.BooleanValueAccessor;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class GameRuleInit {
    private GameRuleInit() {}

    public static final Map<String, GameRules.Key<?>> REGISTRY = new HashMap<>();

    public static GameRules.Key<GameRules.BooleanValue> PORTAL_SLOWSHOT;
    public static GameRules.Key<GameRules.BooleanValue> USE_PORTALABLE_BLACKLIST;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_PORTAL_OVERWRITE;

    public static void registerAll() {
        PORTAL_SLOWSHOT = registerBoolean("portalSlowShot", GameRules.Category.PLAYER, false);
        USE_PORTALABLE_BLACKLIST = registerBoolean("usePortalableBlacklist", GameRules.Category.PLAYER, false);
        // Server-only: the client does not need to know this value. The server consults it at
        // portal-placement time and either evicts the foreign portal in the way or rejects the
        // shot; no client-side behavior (rendering, UI, prediction) depends on it.
        ALLOW_PORTAL_OVERWRITE = registerServerBoolean("allowPortalOverwrite", GameRules.Category.PLAYER, true);
    }

    private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> rule) {
        GameRules.Key<T> key = GameRules.register(name, category, rule);
        REGISTRY.put(name, key);
        return key;
    }

    private static GameRules.Key<GameRules.BooleanValue> registerBoolean(String name, GameRules.Category category, boolean defaultValue) {
        return register(name, category, BooleanValueAccessor.pmCreate(defaultValue, (server, value) ->
                PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SUpdateBooleanGameRulePacket(name, value.get()))));
    }

    private static GameRules.Key<GameRules.BooleanValue> registerServerBoolean(String name, GameRules.Category category, boolean defaultValue) {
        return register(name, category, BooleanValueAccessor.pmCreate(defaultValue, (server, value) -> {}));
    }

    @SuppressWarnings("unchecked")
    public static <T extends GameRules.Value<T>> GameRules.Key<T> getRule(String name) {
        return (GameRules.Key<T>)REGISTRY.get(name);
    }

    public static void sendBooleanRule(ServerPlayer player, GameRules.Key<GameRules.BooleanValue> key) {
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new SUpdateBooleanGameRulePacket(key.getId(), player.level().getGameRules().getBoolean(key)));
    }
}