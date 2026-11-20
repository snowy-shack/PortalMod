package net.portalmod.common.sorted.trigger;

import net.portalmod.core.init.TileEntityTypeInit;

import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

public class TriggerTileEntity extends BlockEntity implements TickableBlockEntity {
    private BlockPos fieldStart;
    private BlockPos fieldEnd;
    private TriggerType type = TriggerType.PLAYER;

    private int entityCount = 0;

    private Player configuringPlayer;

    public TriggerTileEntity(BlockEntityType<?> type) {
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

        AABB aabb = this.getField();
        aabb = aabb.move(this.worldPosition);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb,
                state.getValue(TriggerBlock.TYPE).getPredicate().and(entity -> !entity.isSpectator()));

        if (this.entityCount != entities.size()) {
            this.entityCount = entities.size();
            this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
        }

        boolean shouldActivate = !this.isBeingConfigured() && !entities.isEmpty();

        if (!state.getValue(TriggerBlock.STATE).isActive(shouldActivate)) {
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TriggerBlock.STATE, TriggerState.fromActive(shouldActivate)));
        }
    }

    public void startConfiguration(ServerPlayer player) {
        this.configuringPlayer = player;
    }

    public void endConfiguration() {
        this.configuringPlayer = null;
    }

    public boolean isBeingConfigured() {
        return this.configuringPlayer != null;
    }

    public Player getConfiguringPlayer() {
        return this.configuringPlayer;
    }

    public void setField(BlockPos start, BlockPos end) {
        this.fieldStart = start;
        this.fieldEnd = end;
    }

    public boolean hasField() {
        return this.fieldStart != null && this.fieldEnd != null;
    }

    public AABB getField() {
        if(!this.hasField())
            return null;
        return new AABB(this.fieldStart, this.fieldEnd).expandTowards(1, 1, 1);
    }

    public TriggerType getTriggerType() {
        return this.type;
    }

    public void updateTriggerType() {
        if (this.level == null) return;
        this.type = this.level.getBlockState(this.getBlockPos()).getValue(TriggerBlock.TYPE);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        if(this.fieldStart != null && this.fieldEnd != null) {
            CompoundTag start = new CompoundTag();
            start.putInt("x", this.fieldStart.getX());
            start.putInt("y", this.fieldStart.getY());
            start.putInt("z", this.fieldStart.getZ());
            nbt.put("start", start);

            CompoundTag end = new CompoundTag();
            end.putInt("x", this.fieldEnd.getX());
            end.putInt("y", this.fieldEnd.getY());
            end.putInt("z", this.fieldEnd.getZ());
            nbt.put("end", end);
        }

        return super.save(nbt);
    }
    
    @Override
    public void load(BlockState state, CompoundTag nbt) {
        super.load(state, nbt);
        load(nbt);

        updateTriggerType();
    }
    
    public void load(CompoundTag nbt) {
        if(nbt.contains("start") && nbt.contains("end")) {
            CompoundTag start = nbt.getCompound("start");
            CompoundTag end = nbt.getCompound("end");

            if(!start.contains("x") || !start.contains("y") || !start.contains("z"))
                return;
            if(!end.contains("x") || !end.contains("y") || !end.contains("z"))
                return;

            this.fieldStart = new BlockPos(start.getInt("x"), start.getInt("y"), start.getInt("z"));
            this.fieldEnd = new BlockPos(end.getInt("x"), end.getInt("y"), end.getInt("z"));
        }
    }

    @Override
    public void rotate(Rotation rotation) {
        this.fieldStart = this.fieldStart.rotate(rotation);
        this.fieldEnd = this.fieldEnd.rotate(rotation);
    }

    @Override
    public void mirror(Mirror mirror) {
        int flipX = mirror == Mirror.FRONT_BACK ? -1 : 1;
        int flipZ = mirror == Mirror.LEFT_RIGHT ? -1 : 1;
        this.fieldStart = new BlockPos(this.fieldStart.getX() * flipX, this.fieldStart.getY(), this.fieldStart.getZ() * flipZ);
        this.fieldEnd = new BlockPos(this.fieldEnd.getX() * flipX, this.fieldEnd.getY(), this.fieldEnd.getZ() * flipZ);
    }

    // chunk update
    
    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }
    
    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        load(state, tag);
    }
    
    // block update
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.getBlockPos(), -1, save(new CompoundTag()));
    }
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.load(packet.getTag());
    }
    
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(1E30);
    }
    
    @Override
    public double getViewDistance() {
        return 256.0D;
    }

    public int getEntityCount() {
        return entityCount;
    }
}