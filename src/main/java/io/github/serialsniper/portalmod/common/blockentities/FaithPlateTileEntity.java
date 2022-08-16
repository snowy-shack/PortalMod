package io.github.serialsniper.portalmod.common.blockentities;

import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class FaithPlateTileEntity extends TileEntity {
    private boolean enabled = false;
    private BlockPos targetPos;
    private Direction targetSide;
    private float height;

    public FaithPlateTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public FaithPlateTileEntity() {
        this(TileEntityTypeInit.FAITHPLATE.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        if(targetPos != null && targetSide != null) {
            CompoundNBT target = new CompoundNBT();
            target.putInt("x", targetPos.getX());
            target.putInt("y", targetPos.getY());
            target.putInt("z", targetPos.getZ());
            target.putByte("side", (byte) targetSide.get3DDataValue());
            target.putFloat("height", height);
            nbt.put("target", target);
        } else {
            enabled = false;
        }

        nbt.putBoolean("enabled", enabled);
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        load(nbt);
    }

    public void load(CompoundNBT nbt) {
        if(nbt.contains("enabled"))
            enabled = nbt.getBoolean("enabled");
        else
            enabled = false;

        if(enabled && nbt.contains("target")) {
            CompoundNBT target = nbt.getCompound("target");
            if(target.contains("x") && target.contains("y") && target.contains("z") && target.contains("side") && target.contains("height")) {
                int x = target.getInt("x");
                int y = target.getInt("y");
                int z = target.getInt("z");
                targetPos = new BlockPos(x, y, z);
                targetSide = Direction.from3DDataValue(target.getByte("side"));
                height = target.getFloat("height");
            } else {
                enabled = false;
            }
        } else {
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public Direction getTargetSide() {
        return targetSide;
    }

    public float getHeight() {
        return height;
    }

    // chunk update

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        load(state, tag);
    }

    // block update

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.getBlockPos(), -1, save(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.load(packet.getTag());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public double getViewDistance() {
        return 256.0D;
    }
}