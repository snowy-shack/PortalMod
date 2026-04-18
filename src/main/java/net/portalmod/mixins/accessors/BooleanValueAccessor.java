package net.portalmod.mixins.accessors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;

@Mixin(GameRules.BooleanValue.class)
public interface BooleanValueAccessor {
    @Invoker(value = "create")
    static GameRules.RuleType<GameRules.BooleanValue> pmCreate(boolean value, BiConsumer<MinecraftServer, GameRules.BooleanValue> consumer) {
        throw new AssertionError();
    }
}