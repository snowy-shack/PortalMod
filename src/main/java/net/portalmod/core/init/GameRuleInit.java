package net.portalmod.core.init;

import net.minecraft.world.GameRules;
import net.portalmod.mixins.accessors.BooleanValueAccessor;

import java.util.ArrayList;
import java.util.List;

public class GameRuleInit {
    private GameRuleInit() {}

    public static final List<GameRules.RuleKey<?>> REGISTRY = new ArrayList<>();

    public static GameRules.RuleKey<GameRules.BooleanValue> DO_FUNNELING;

    public static void registerAll() {
        DO_FUNNELING = GameRules.register("portalFunneling", GameRules.Category.PLAYER, BooleanValueAccessor.pmCreate(true));

        REGISTRY.add(DO_FUNNELING);
    }
}