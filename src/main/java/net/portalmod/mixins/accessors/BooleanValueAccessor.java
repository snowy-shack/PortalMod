package net.portalmod.mixins.accessors;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;

import net.minecraft.world.level.GameRules;

@Mixin(GameRules.BooleanValue.class)
public interface BooleanValueAccessor {
    @Invoker(value = "create")
    static GameRules.Type<GameRules.BooleanValue> pmCreate(boolean value, BiConsumer<MinecraftServer, GameRules.BooleanValue> consumer) {
        throw new AssertionError();
    }
}