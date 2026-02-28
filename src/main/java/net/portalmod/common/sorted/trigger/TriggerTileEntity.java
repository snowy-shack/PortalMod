package net.portalmod.common.sorted.trigger;

import net.minecraft.block.BlockState;
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
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;

import java.util.HashMap;

public class TriggerTileEntity extends TileEntity implements ITickableTileEntity {
    private BlockPos fieldStart;
    private BlockPos fieldEnd;

    private static final HashMap<PlayerEntity, TriggerTileEntity> TRIGGER_PER_PLAYER = new HashMap<>();
    private PlayerEntity configuringPlayer;
    public static TriggerTileEntity selected;
    public static BlockPos selectingStart;
    public static BlockPos selectingEnd;

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

        // todo limit selection distance

        if(this.hasField()) {
            AxisAlignedBB aabb = this.getField();
            aabb = aabb.move(this.worldPosition);

            boolean shouldActivate = !this.isBeingConfigured() && !level.getEntitiesOfClass(PlayerEntity.class, aabb).isEmpty();
            BlockState state = this.level.getBlockState(this.worldPosition);

            if (state.getValue(TriggerBlock.ACTIVATED) != shouldActivate) {
                this.level.setBlock(this.worldPosition, state.setValue(TriggerBlock.ACTIVATED, shouldActivate), 3);
            }
        }
    }

    public void startConfiguration(ServerPlayerEntity player) {
        this.configuringPlayer = player;
        TRIGGER_PER_PLAYER.put(player, this);
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new STriggerStartConfigPacket(this.getBlockPos()));
    }

    public void endConfiguration() {
        endConfigurationForPlayer(this.configuringPlayer);
    }

    public static void endConfigurationForPlayer(PlayerEntity player) {
        if(TRIGGER_PER_PLAYER.containsKey(player))
            TRIGGER_PER_PLAYER.get(player).configuringPlayer = null;
        TRIGGER_PER_PLAYER.remove(player);
    }

    public boolean isBeingConfigured() {
        return this.configuringPlayer != null;
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
}