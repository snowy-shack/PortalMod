package net.portalmod.core.init;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.portalmod.PortalMod;
import org.lwjgl.glfw.GLFW;

public class KeyInit {

    private KeyInit() {}

    public static KeyBinding PORTALGUN_INTERACT;

    public static void init() {
        PORTALGUN_INTERACT = new KeyBinding(
                "key." + PortalMod.MODID + ".portalgun_interact",
                GLFW.GLFW_KEY_E,
                "key.category." + PortalMod.MODID
        );

        ClientRegistry.registerKeyBinding(PORTALGUN_INTERACT);
    }
}