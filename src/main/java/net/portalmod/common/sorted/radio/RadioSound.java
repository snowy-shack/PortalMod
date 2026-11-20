package net.portalmod.common.sorted.radio;

import net.minecraft.util.RandomSource;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;

public class RadioSound extends AbstractSoundInstance {
    public RadioSound(BlockPos pos, SoundEvent sound, boolean looping) {
        super(sound, SoundSource.RECORDS, RandomSource.create());
        x = pos.getX() + .5;
        y = pos.getY() + .5;
        z = pos.getZ() + .5;
        this.looping = looping;
        this.volume = .5f;
    }
}