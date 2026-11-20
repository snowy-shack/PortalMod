package net.portalmod.core.util;

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
        boolean isClientSide = PortalMod.getServer() == null || !PortalMod.getServer().isSameThread();

        ModUtil.sendChat(clientWorld, isClientSide, text);
    }

    public static Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

    /** Builds the context entity renderers need, for renderers created outside the renderer registry. */
    public static net.minecraft.client.renderer.entity.EntityRendererProvider.Context entityRendererContext() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        return new net.minecraft.client.renderer.entity.EntityRendererProvider.Context(mc.getEntityRenderDispatcher(),
                mc.getItemRenderer(), mc.getBlockRenderer(), mc.gameRenderer.itemInHandRenderer,
                mc.getResourceManager(), mc.getEntityModels(), mc.font);
    }
}
