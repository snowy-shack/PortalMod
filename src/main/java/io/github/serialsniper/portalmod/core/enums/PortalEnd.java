package io.github.serialsniper.portalmod.core.enums;

import net.minecraft.util.*;

public enum PortalEnd implements IStringSerializable {
	BLUE,
	ORANGE,
	NONE;
	
	public String toString() {
		return this.getSerializedName();
	}
	
	public String getSerializedName() {
		return this == BLUE ? "blue" : (this == ORANGE ? "orange" : "none");
	}
	
	public PortalEnd other() {
		if(this == BLUE)
			return ORANGE;
		if(this == ORANGE)
			return BLUE;
		return NONE;
	}
}