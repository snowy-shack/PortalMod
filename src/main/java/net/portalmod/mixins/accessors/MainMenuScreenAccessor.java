package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.resources.ResourceLocation;

@Mixin(TitleScreen.class)
public interface MainMenuScreenAccessor {
    @Accessor(value = "MINECRAFT_EDITION")
    static void pmSetEdition(ResourceLocation value) {
        throw new AssertionError();
    }

    @Accessor(value = "CUBE_MAP")
    static void pmSetCubeMap(CubeMap value) {
        throw new AssertionError();
    }

    @Accessor(value = "MINECRAFT_EDITION")
    static ResourceLocation pmGetEdition() {
        throw new AssertionError();
    }

    @Accessor(value = "CUBE_MAP")
    static CubeMap pmGetCubeMap() {
        throw new AssertionError();
    }
}