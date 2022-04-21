package io.github.serialsniper.portalmod.client.render;

import com.mojang.blaze3d.matrix.*;
import net.minecraft.client.*;
import net.minecraft.client.util.*;
import net.minecraft.util.math.vector.*;
import org.lwjgl.glfw.*;

public class ManualTransformer {
    private static Vector3f translation = new Vector3f(0, 0, 0);
    private static int rotX = 0;
    private static int rotY = 0;
    private static int rotZ = 0;

    private static final int cooldown = 10;
    private static boolean cooldownEnabled = false;
    private static int cooldownCount = 0;

    public static void apply(MatrixStack transform) {
        transform.translate(translation.x(), translation.y(), translation.z());
        transform.mulPose(Vector3f.XP.rotationDegrees(rotX));
        transform.mulPose(Vector3f.YP.rotationDegrees(rotY));
        transform.mulPose(Vector3f.ZP.rotationDegrees(rotZ));
    }

    private static void transform() {
        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_END)) {
            translation = new Vector3f(0, 0, 0);
            rotX = 0;
            rotY = 0;
            rotZ = 0;
        }

        if(cooldownEnabled) {
            if(cooldownCount++ >= cooldown) {
                cooldownEnabled = false;
                cooldownCount = 0;
            }
            return;
        }

        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_UP)) {
            translation.add(0, 0.1f, 0);
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_DOWN)) {
            translation.add(0, -0.1f, 0);
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT)) {
            translation.add(0.1f, 0, 0);
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT)) {
            translation.add(-0.1f, 0, 0);
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_PAGE_DOWN)) {
            translation.add(0, 0, 0.1f);
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_PAGE_UP)) {
            translation.add(0, 0, -0.1f);
            cooldownEnabled = true;
        }

        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_KP_8)) {
            rotX++;
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_KP_2)) {
            rotX--;
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_KP_6)) {
            rotY++;
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_KP_4)) {
            rotY--;
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_KP_9)) {
            rotZ++;
            cooldownEnabled = true;
        } if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_KP_7)) {
            rotZ--;
            cooldownEnabled = true;
        }
    }
}