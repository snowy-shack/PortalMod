package io.github.serialsniper.portalmod.core.util;

import javax.annotation.*;

import net.minecraft.util.math.*;

public class PortalPair<Blue, Orange> {
	private BlockPos blue, orange;
	
	public PortalPair(@Nullable BlockPos blue, @Nullable BlockPos orange) {
		this.blue = blue;
		this.orange = orange;
	}
	
	public BlockPos getBlue() {
		return blue;
	}
	
	public BlockPos getOrange() {
		return orange;
	}
	
	public void setBlue(BlockPos blue) {
		this.blue = blue;
	}
	
	public void setOrange(BlockPos orange) {
		this.orange = orange;
	}
}