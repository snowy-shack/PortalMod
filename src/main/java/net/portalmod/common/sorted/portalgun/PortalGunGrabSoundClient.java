package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.portalmod.core.init.SoundInit;

import java.util.HashMap;
import java.util.Map;

public class PortalGunGrabSoundClient {
    protected static final Map<PlayerEntity, LocatableSound> SOUNDS = new HashMap<>();

    // TODO this is not a real packet
    public static void handlePacket(PlayerEntity player, boolean start) {
        if (start) {
            SOUNDS.put(player, new EntityLoopableSound(player, SoundInit.PORTALGUN_HOLD.get(), SoundCategory.PLAYERS));
            Minecraft.getInstance().getSoundManager().play(SOUNDS.get(player));
        } else {
            Minecraft.getInstance().getSoundManager().stop(SOUNDS.get(player));
        }
    }
}