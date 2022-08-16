package io.github.serialsniper.portalmod.common.blockentities;

import io.github.serialsniper.portalmod.common.RadioSound;
import io.github.serialsniper.portalmod.common.blocks.RadioBlock;
import io.github.serialsniper.portalmod.core.enums.RadioState;
import io.github.serialsniper.portalmod.core.init.PacketInit;
import io.github.serialsniper.portalmod.core.init.SoundInit;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import io.github.serialsniper.portalmod.core.packet.RadioUpdateClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class RadioBlockTileEntity extends TileEntity implements ITickableTileEntity {
	private RadioSound sound;
	private int countedTicks = 0;
	private boolean initialized = false;

	public RadioBlockTileEntity() {
		super(TileEntityTypeInit.RADIO.get());
	}

	public void setInitialized() {
		initialized = true;
	}

	@Override
	public void tick() {
		if(level.isClientSide())
			return;

		if(!initialized) {
			if(!getBlockState().getValue(RadioBlock.POWERED))
				setState(RadioState.OFF);
			initialized = true;
		}

		if(getState() == RadioState.ACTIVE) {
			if(countedTicks++ == 900) {
				setState(RadioState.INACTIVE);
				countedTicks = 0;
			}
		} else countedTicks = 0;
	}

	public void switchManual() {
		if(level.isClientSide() || getBlockState().getValue(RadioBlock.POWERED) || getState() == RadioState.ACTIVE) return;

		if(isPlaying())
			stop();
		else
			play();
	}

	public void handlePacket(RadioState state) {
		if(!state.isPlaying()) {
			Minecraft.getInstance().getSoundManager().stop(sound);
		} else {
			if(state == RadioState.ACTIVE)
				sound = new RadioSound(this.getBlockPos(), SoundInit.RADIO_DINOSAUR1.get(), false);
			else if(state == RadioState.ON)
				sound = new RadioSound(this.getBlockPos(), SoundInit.RADIO_LOOP.get(), true);
			Minecraft.getInstance().getSoundManager().play(sound);
		}
	}

	public void play() {
		if(level.isClientSide() || isPlaying()) return;

		if(getLevel().dimension() != null && getLevel().dimension() == World.END)
			setState(RadioState.ACTIVE);
		else
			setState(RadioState.ON);
		sendUpdatePacket();
	}
	
	public void stop() {
		if(level.isClientSide() || !isPlaying()) return;

		if(getLevel().dimension() != null && getLevel().dimension() == World.END)
			setState(RadioState.INACTIVE);
		else
			setState(RadioState.OFF);
		sendUpdatePacket();
	}

	public void sendUpdatePacket() {
		PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
				() -> this.level.getChunkAt(getBlockPos())),
				new RadioUpdateClientPacket(getBlockPos(), getState()));
	}

	private RadioState getState() {
		return getBlockState().getValue(RadioBlock.STATE);
	}

	private void setState(RadioState state) {
		getLevel().setBlock(getBlockPos(), getBlockState().setValue(RadioBlock.STATE, state), 2);
	}

	public boolean isPlaying() {
		return getState().isPlaying();
	}

	@Override
	public void setRemoved() {
		if(level.isClientSide())
			handlePacket(RadioState.OFF);
		super.setRemoved();
	}
}