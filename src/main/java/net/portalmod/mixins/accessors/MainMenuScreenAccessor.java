package net.portalmod.mixins.accessors;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets the mod swap the title screen panorama (the field is static final, hence @Mutable). */
@Mixin(TitleScreen.class)
public interface MainMenuScreenAccessor {
    @Mutable
    @Accessor(value = "CUBE_MAP")
    static void pmSetCubeMap(CubeMap value) {
        throw new AssertionError();
    }

    @Accessor(value = "CUBE_MAP")
    static CubeMap pmGetCubeMap() {
        throw new AssertionError();
    }
}
