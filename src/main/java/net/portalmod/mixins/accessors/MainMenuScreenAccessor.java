package net.portalmod.mixins.accessors;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MainMenuScreen.class)
public interface MainMenuScreenAccessor {
    @Accessor(value = "MINECRAFT_EDITION")
    static void pmSetEdition(ResourceLocation value) {
        throw new AssertionError();
    }

    @Accessor(value = "CUBE_MAP")
    static void pmSetCubeMap(RenderSkyboxCube value) {
        throw new AssertionError();
    }

    @Accessor(value = "MINECRAFT_EDITION")
    static ResourceLocation pmGetEdition() {
        throw new AssertionError();
    }

    @Accessor(value = "CUBE_MAP")
    static RenderSkyboxCube pmGetCubeMap() {
        throw new AssertionError();
    }
}