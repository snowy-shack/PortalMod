package net.portalmod.core.init;

import net.minecraft.world.GameRules;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.packet.SUpdatePortalableGameRulePacket;
import net.portalmod.mixins.accessors.BooleanValueAccessor;

import java.util.ArrayList;
import java.util.List;

public class GameRuleInit {
    private GameRuleInit() {}

    public static final List<GameRules.RuleKey<?>> REGISTRY = new ArrayList<>();

    public static GameRules.RuleKey<GameRules.BooleanValue> DO_FUNNELING;
    public static GameRules.RuleKey<GameRules.BooleanValue> USE_PORTALABLE_BLACKLIST;

    public static void registerAll() {
        DO_FUNNELING = GameRules.register("portalFunneling", GameRules.Category.PLAYER, BooleanValueAccessor.pmCreate(true));

        USE_PORTALABLE_BLACKLIST = GameRules.register("usePortalableBlacklist", GameRules.Category.PLAYER,
                BooleanValueAccessor.pmCreate(false, (server, value) ->
                server.getPlayerList().getPlayers().forEach(player ->
                        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                new SUpdatePortalableGameRulePacket(value.get())))));

        REGISTRY.add(DO_FUNNELING);
        REGISTRY.add(USE_PORTALABLE_BLACKLIST);
    }
}