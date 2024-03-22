package net.portalmod.common.sorted.radio;

import net.minecraft.client.Minecraft;
import net.portalmod.core.init.SoundInit;

import java.util.HashMap;
import java.util.Map;

public class RadioBlockClient {
    protected static final Map<RadioBlockTileEntity, RadioSound> RADIOS = new HashMap<>();

    protected static void handlePacket(RadioBlockTileEntity be, RadioState state) {
        if(!state.isPlaying()) {
            Minecraft.getInstance().getSoundManager().stop(RADIOS.get(be));
        } else {
            if(state == RadioState.ACTIVE)
                RADIOS.put(be, new RadioSound(be.getBlockPos(), SoundInit.RADIO_DINOSAUR1.get(), false));
            else if(state == RadioState.ON)
                RADIOS.put(be, new RadioSound(be.getBlockPos(), SoundInit.RADIO_LOOP.get(), true));
            Minecraft.getInstance().getSoundManager().play(RADIOS.get(be));
        }
    }
}