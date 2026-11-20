package net.portalmod.mixins.accessors;

import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets the mod swap the "Java Edition" texture (moved from TitleScreen to LogoRenderer in 1.19.4). */
@Mixin(LogoRenderer.class)
public interface LogoRendererAccessor {
    @Mutable
    @Accessor(value = "MINECRAFT_EDITION")
    static void pmSetEdition(ResourceLocation value) {
        throw new AssertionError();
    }

    @Accessor(value = "MINECRAFT_EDITION")
    static ResourceLocation pmGetEdition() {
        throw new AssertionError();
    }
}
