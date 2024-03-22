package net.portalmod.core.init;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.portalmod.PortalMod;

import java.awt.event.KeyEvent;

public class KeyInit {
    private KeyInit() {}
    public static void init() {}
    
    public static final KeyBinding PORTALGUN_INTERACT = registerKey("portalgun_interact", KeyEvent.VK_E);

    private static KeyBinding registerKey(String name, int keycode) {
        final KeyBinding key = new KeyBinding("key." + PortalMod.MODID + "." + name, keycode, "key.category." + PortalMod.MODID);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}