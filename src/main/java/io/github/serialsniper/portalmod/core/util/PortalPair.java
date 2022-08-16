package io.github.serialsniper.portalmod.core.util;

import javax.annotation.*;

import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;

public class PortalPair {
	private PortalEntity blue, orange;
	
	public boolean has(PortalEnd end) {
		return this.get(end) != null;
	}
	
	@Nullable
	public PortalEntity get(PortalEnd end) {
		if(end == PortalEnd.BLUE)
			return blue;
		if(end == PortalEnd.ORANGE)
			return orange;
		return null;
	}
	
	public boolean isEmpty() {
		return blue == null && orange == null;
	}
	
	public boolean isFull() {
		return blue != null && orange != null;
	}
	
	public void set(PortalEnd end, PortalEntity entity) {
		if(end == PortalEnd.BLUE)
			this.blue = entity;
		if(end == PortalEnd.ORANGE)
			this.orange = entity;
	}
	
	public void remove(PortalEntity portal) {
		if(this.blue == portal)
			this.blue = null;
		if(this.orange == portal)
			this.orange = null;
	}
}