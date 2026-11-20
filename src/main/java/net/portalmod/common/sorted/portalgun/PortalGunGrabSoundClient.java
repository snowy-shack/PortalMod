package net.portalmod.common.sorted.portalgun;

import net.portalmod.core.init.SoundInit;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;

public class PortalGunGrabSoundClient {
    protected static final Map<Player, AbstractSoundInstance> SOUNDS = new HashMap<>();

    // TODO this is not a real packet
    public static void handlePacket(Player player, boolean start) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        boolean hasSound = SOUNDS.containsKey(player);

        if (start && !hasSound) {
            SOUNDS.put(player, new EntityLoopableSound(player, SoundInit.PORTALGUN_HOLD.get(), SoundSource.PLAYERS));
            soundManager.play(SOUNDS.get(player));
        }

        if (!start && hasSound) {
            soundManager.stop(SOUNDS.get(player));
            SOUNDS.remove(player);
        }
    }
}