package io.github.serialsniper.portalmod.core.enums;

import net.minecraft.util.*;

public enum PortalEnd implements IStringSerializable {
	NONE("none"),
	BLUE("blue"),
	ORANGE("orange");

	private final String name;

	PortalEnd(String name) {
		this.name = name;
	}
	public String toString() {
		return this.getSerializedName();
	}
	public String getSerializedName() {
		return name;
	}
	public PortalEnd other() {
		if(this == BLUE)
			return ORANGE;
		if(this == ORANGE)
			return BLUE;
		return NONE;
	}
}