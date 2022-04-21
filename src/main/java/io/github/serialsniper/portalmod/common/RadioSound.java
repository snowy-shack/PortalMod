package io.github.serialsniper.portalmod.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

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