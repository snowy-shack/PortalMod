package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.common.entity.FizzleableEntity;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CubeDropperTileEntity extends TileEntity implements ITickableTileEntity {

    public List<UUID> entityUUIDs = new ArrayList<>();
    public int openTicks = 0;
    public boolean wasPowered = false;
    public EntityType<? extends Entity> entityType = null;

    public CubeDropperTileEntity() {
        super(TileEntityTypeInit.CUBE_DROPPER.get());
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();
        if (!(blockState.getBlock() instanceof CubeDropperBlock) || this.entityType == null) {
            return;
        }

        if (this.openTicks > 0) {
            this.openTicks++;
        }

        this.updateEntities();

        CubeDropperBlock dropperBlock = (CubeDropperBlock) blockState.getBlock();

        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(this.getBlockPos()));
        possibleIndicatorPositions.addAll(getSurroundingPositions(this.getBlockPos().below()));

        boolean hasIndicators = false;
        boolean allIndicatorsActivated = true;

        for (BlockPos blockPos : possibleIndicatorPositions) {
            BlockState worldBlockState = this.level.getBlockState(blockPos);
            if (worldBlockState.getBlock() instanceof AntlineIndicatorBlock) {
                hasIndicators = true;
                if (!worldBlockState.getValue(AntlineIndicatorBlock.ACTIVE)) {
                    allIndicatorsActivated = false;
                }
            }
        }

        boolean isPowered = blockState.getValue(CubeDropperBlock.POWERED) || hasIndicators && allIndicatorsActivated;

        if (isPowered && this.openTicks == 0 && (this.entityUUIDs.size() == 1 || !this.wasPowered)) {
            openDropper(dropperBlock);
        }

        this.wasPowered = isPowered;

        if (this.openTicks > 20) {
            closeDropper(dropperBlock);
        }
    }

    // Open dropper and fizzle first cube
    public void openDropper(CubeDropperBlock dropperBlock) {
        dropperBlock.setOpen(true, this.getBlockState(), this.level, this.getBlockPos());

        if (this.level instanceof ServerWorld) {
            this.openTicks = 1;
            if (this.entityUUIDs.size() == 2) {
                Entity entity = ((ServerWorld) this.level).getEntity(this.entityUUIDs.get(0));
                if (entity instanceof FizzleableEntity) {
                    ((FizzleableEntity) entity).startFizzling();
                } else {
                    entity.remove();
                }
                this.entityUUIDs.remove(0);
            }
        }
    }

    // Close dropper and spawn a new cube inside the dropper
    public void closeDropper(CubeDropperBlock dropperBlock) {
        dropperBlock.setOpen(false, this.getBlockState(), this.level, this.getBlockPos());

        if (this.level instanceof ServerWorld) {
            this.openTicks = 0;
            addEntity();
        }
    }

    public void updateEntities() {
        if (this.entityUUIDs.isEmpty()) {
            addEntity();
        }
        this.entityUUIDs.removeIf(uuid -> this.level instanceof ServerWorld && ((ServerWorld) this.level).getEntity(uuid) == null);
    }

    public void addEntity() {
        if (this.entityType == null) {
            return;
        }

        Entity entity = this.entityType.create(this.level);
        Vector3f position = new Vec3(this.getBlockPos().south().east()).to3f();
        entity.teleportTo(position.x(), position.y(), position.z());
        this.entityUUIDs.add(entity.getUUID());
        this.level.addFreshEntity(entity);
    }

    public void removeAllEntities() {
        if (this.level instanceof ServerWorld) {
            for (UUID uuid : this.entityUUIDs) {
                Entity entity = ((ServerWorld) this.level).getEntity(uuid);
                if (entity != null) {
                    if (entity instanceof FizzleableEntity) {
                        ((FizzleableEntity) entity).startFizzling();
                    } else {
                        entity.remove();
                    }
                }
            }
            this.entityUUIDs.clear();
        }
    }

    /*

    5      6       7      8
    4   updated  dropper  9
    3   dropper  dropper  10
    2      1       12     11

     */
    public static List<BlockPos> getSurroundingPositions(BlockPos pos) {
        return new ArrayList<>(Arrays.asList(
                pos.south(2),
                pos.south(2).west(),
                pos.south().west(),
                pos.west(),
                pos.north().west(),
                pos.north(),
                pos.north().east(),
                pos.north().east(2),
                pos.east(2),
                pos.south().east(2),
                pos.south(2).east(2),
                pos.south(2).east()
        ));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);
        if (!this.entityUUIDs.isEmpty()) {
            nbt.putUUID("UUID1", this.entityUUIDs.get(0));
        }
        if (this.entityUUIDs.size() >= 2) {
            nbt.putUUID("UUID2", this.entityUUIDs.get(1));
        }
        nbt.putBoolean("Powered", this.wasPowered);
        nbt.putInt("OpenTicks", this.openTicks);
        nbt.putString("EntityType", this.entityType == null ? "null" : this.entityType.getRegistryName().toString());
        return nbt;
    }

    @Override
    public void load(BlockState blockState, CompoundNBT nbt) {
        super.load(blockState, nbt);
        this.entityUUIDs.clear();
        if (nbt.hasUUID("UUID1")) {
            this.entityUUIDs.add(nbt.getUUID("UUID1"));
        }
        if (nbt.hasUUID("UUID2")) {
            this.entityUUIDs.add(nbt.getUUID("UUID2"));
        }
        this.wasPowered = nbt.getBoolean("Powered");
        this.openTicks = nbt.getInt("OpenTicks");
        String entityType = nbt.getString("EntityType");
        this.entityType = entityType.equals("null") ? null : ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityType));
    }
}
