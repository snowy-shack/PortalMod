package io.github.serialsniper.portalmod.common.blockentities;

import java.util.*;

import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.block.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;

public class PortalableBlockTileEntity extends TileEntity implements ITickableTileEntity {
	public UUID uuid;

	public PortalableBlockTileEntity() {
		super(TileEntityTypeInit.PORTABLE_BLOCK.get());
	}

	@Override
	public void tick() {}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		return super.save(nbt);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getBlockPos(), getBlockPos().offset(1, 2, 1));
	}
}