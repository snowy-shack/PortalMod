package io.github.serialsniper.portalmod.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;

public class InputUtil {
    public static boolean isKeyDown(int key) {
        return InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }
}