package net.portalmod.mixins.hooks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Replaces Forge's GuiOpenEvent (used to swap the title screen). */
@Mixin(Minecraft.class)
public abstract class MinecraftHooksMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen pmOnSetScreen(Screen screen) {
        return ClientEvents.screenOpen(screen);
    }
}
