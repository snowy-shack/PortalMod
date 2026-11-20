package net.portalmod.core.init;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.portalmod.PortalMod;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

public class KeyInit {

    private KeyInit() {}

    public static KeyMapping PORTALGUN_INTERACT;

    public static void init() {
        PORTALGUN_INTERACT = new KeyMapping(
                "key." + PortalMod.MODID + ".portalgun_interact",
                GLFW.GLFW_KEY_E,
                "key.category." + PortalMod.MODID
        );

        KeyBindingHelper.registerKeyBinding(PORTALGUN_INTERACT);
    }
}