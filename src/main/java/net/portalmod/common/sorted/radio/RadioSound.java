package net.portalmod.common.sorted.radio;

import net.minecraft.client.audio.LocatableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class RadioSound extends LocatableSound {
    public RadioSound(BlockPos pos, SoundEvent sound, boolean looping) {
        super(sound, SoundCategory.RECORDS);
        x = pos.getX() + .5;
        y = pos.getY() + .5;
        z = pos.getZ() + .5;
        this.looping = looping;
        this.volume = .5f;
    }
}