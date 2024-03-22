package net.portalmod.common.sorted.radio;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;

public class RadioBlockTileEntity extends TileEntity implements ITickableTileEntity {
//    private RadioSound sound;
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
        if(this.level.isClientSide())
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
        if(this.level.isClientSide() || getBlockState().getValue(RadioBlock.POWERED) || getState() == RadioState.ACTIVE)
            return;

        if(isPlaying())
            stop();
        else
            play();
    }

    public void handlePacket(RadioState state) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RadioBlockClient.handlePacket(this, state));
    }

    public void play() {
        if(this.level.isClientSide() || isPlaying()) return;

        if(this.level.dimension() != null && getLevel().dimension() == World.END)
            setState(RadioState.ACTIVE);
        else
            setState(RadioState.ON);
        sendUpdatePacket();
    }
    
    public void stop() {
        if(this.level.isClientSide() || !isPlaying()) return;

        if(this.level.dimension() != null && getLevel().dimension() == World.END)
            setState(RadioState.INACTIVE);
        else
            setState(RadioState.OFF);
        sendUpdatePacket();
    }

    public void sendUpdatePacket() {
        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                () -> this.level.getChunkAt(getBlockPos())),
                    new SRadioUpdatePacket(getBlockPos(), getState()));
    }

    private RadioState getState() {
        return getBlockState().getValue(RadioBlock.STATE);
    }

    private void setState(RadioState state) {
        this.level.setBlock(getBlockPos(), getBlockState().setValue(RadioBlock.STATE, state), 2);
    }

    public boolean isPlaying() {
        return getState().isPlaying();
    }

    @Override
    public void setRemoved() {
        if(this.level.isClientSide())
            handlePacket(RadioState.OFF);
        super.setRemoved();
    }
}