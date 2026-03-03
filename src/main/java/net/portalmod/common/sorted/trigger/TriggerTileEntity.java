package net.portalmod.common.sorted.trigger;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.portalmod.core.init.TileEntityTypeInit;

import java.util.List;

public class TriggerTileEntity extends TileEntity implements ITickableTileEntity {
    private BlockPos fieldStart;
    private BlockPos fieldEnd;

    private int entityCount = 0;

    private PlayerEntity configuringPlayer;

    public TriggerTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public TriggerTileEntity() {
        this(TileEntityTypeInit.TRIGGER.get());
    }
    
    @Override
    public void tick() {
        if(this.level == null)
            return;

        BlockState state = this.level.getBlockState(this.worldPosition);

        if (!this.hasField()) {
            if (state.getValue(TriggerBlock.STATE) != TriggerState.NULL) {
                this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TriggerBlock.STATE, TriggerState.NULL));
            }
            this.entityCount = 0;
            return;
        }

        // todo limit selection distance

        AxisAlignedBB aabb = this.getField();
        aabb = aabb.move(this.worldPosition);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, state.getValue(TriggerBlock.TYPE).getPredicate());

        if (this.entityCount != entities.size()) {
            this.entityCount = entities.size();
            this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
        }

        boolean shouldActivate = !this.isBeingConfigured() && !entities.isEmpty();

        if (!state.getValue(TriggerBlock.STATE).isActive(shouldActivate)) {
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TriggerBlock.STATE, TriggerState.fromActive(shouldActivate)));
        }
    }

    public void startConfiguration(ServerPlayerEntity player) {
        this.configuringPlayer = player;
    }

    public void endConfiguration() {
        this.configuringPlayer = null;
    }

    public boolean isBeingConfigured() {
        return this.configuringPlayer != null;
    }

    public PlayerEntity getConfiguringPlayer() {
        return this.configuringPlayer;
    }

    public void setField(BlockPos start, BlockPos end) {
        this.fieldStart = start;
        this.fieldEnd = end;
    }

    public boolean hasField() {
        return this.fieldStart != null && this.fieldEnd != null;
    }

    public AxisAlignedBB getField() {
        if(!this.hasField())
            return null;
        return new AxisAlignedBB(this.fieldStart, this.fieldEnd).expandTowards(1, 1, 1);
    }
    
    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        if(this.fieldStart != null && this.fieldEnd != null) {
            CompoundNBT start = new CompoundNBT();
            start.putInt("x", this.fieldStart.getX());
            start.putInt("y", this.fieldStart.getY());
            start.putInt("z", this.fieldStart.getZ());
            nbt.put("start", start);

            CompoundNBT end = new CompoundNBT();
            end.putInt("x", this.fieldEnd.getX());
            end.putInt("y", this.fieldEnd.getY());
            end.putInt("z", this.fieldEnd.getZ());
            nbt.put("end", end);
        }

        return super.save(nbt);
    }
    
    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        load(nbt);
    }
    
    public void load(CompoundNBT nbt) {
        if(nbt.contains("start") && nbt.contains("end")) {
            CompoundNBT start = nbt.getCompound("start");
            CompoundNBT end = nbt.getCompound("end");

            if(!start.contains("x") || !start.contains("y") || !start.contains("z"))
                return;
            if(!end.contains("x") || !end.contains("y") || !end.contains("z"))
                return;

            this.fieldStart = new BlockPos(start.getInt("x"), start.getInt("y"), start.getInt("z"));
            this.fieldEnd = new BlockPos(end.getInt("x"), end.getInt("y"), end.getInt("z"));
        }
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
        return new AxisAlignedBB(this.getBlockPos()).inflate(1E30);
    }
    
    @Override
    public double getViewDistance() {
        return 256.0D;
    }

    public int getEntityCount() {
        return entityCount;
    }
}