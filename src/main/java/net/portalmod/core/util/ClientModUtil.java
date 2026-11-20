package net.portalmod.core.util;

import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.portalmod.PortalMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

public class ClientModUtil {
    public static void sendClientChat(Object... text) {
        ClientLevel clientWorld = Minecraft.getInstance().level;
        if (clientWorld == null) {
            PortalMod.LOGGER.error("Tried to send a client chat message while not in a client environment");
            return;
        }

        // This may not always be accurate
        boolean isClientSide = Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER;

        ModUtil.sendChat(clientWorld, isClientSide, text);
    }

    public static Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }
}