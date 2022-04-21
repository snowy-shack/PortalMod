package io.github.serialsniper.portalmod.core.enums;

import net.minecraft.util.*;

public enum RadioState implements IStringSerializable {
	OFF(false),
	ON(true),
	INACTIVE(false),
	ACTIVE(true);

	private boolean isPlaying;

	RadioState(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public String toString() {
		return this.getSerializedName();
	}
	
	public String getSerializedName() {
		return name().toLowerCase();
	}
}