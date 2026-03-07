package net.portalmod.mixins.accessors;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanValue.class)
public interface BooleanValueAccessor {
    @Invoker(remap = false, value = "create")
    static GameRules.RuleType<GameRules.BooleanValue> pmCreate(boolean value) {
        throw new AssertionError();
    }
}