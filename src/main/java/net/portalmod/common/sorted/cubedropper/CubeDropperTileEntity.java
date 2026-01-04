package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;

import java.util.*;

public class CubeDropperTileEntity extends TileEntity implements ITickableTileEntity, IndicatorActivated {

    public List<UUID> entityUUIDs = new ArrayList<>();
    public int openTicks = 0;
    public boolean wasAntlinePowered = false;
    public CompoundNBT entityNBT = null;

    public CubeDropperTileEntity() {
        super(TileEntityTypeInit.CUBE_DROPPER.get());
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        CubeDropperBlock dropperBlock = (CubeDropperBlock) blockState.getBlock();
        IndicatorInfo indicatorInfo = this.checkIndicators(blockState, this.level, pos);
        boolean antlinePowered = indicatorInfo.hasIndicators && indicatorInfo.allIndicatorsActivated;

        if (this.openTicks > 0) this.openTicks++;

        if (blockState.getBlock() instanceof CubeDropperBlock && this.entityNBT != null) {
            this.updateEntities();

            if (antlinePowered && this.openTicks == 0 && (this.entityUUIDs.size() == 1 || !this.wasAntlinePowered)) {
                openDropper(dropperBlock);
            }
        }

        this.wasAntlinePowered = antlinePowered;

        if (this.openTicks > 20) {
            closeDropper(dropperBlock);
        }
    }

    // Open dropper and fizzle first cube
    public void openDropper(CubeDropperBlock dropperBlock) {
        if (this.level instanceof ServerWorld) {
            dropperBlock.setOpen(true, this.getBlockState(), this.level, this.getBlockPos());
            this.openTicks = 1;
            if (this.entityUUIDs.size() == 2) {
                fizzleFirstCube();
            }
        }
    }

    public void fizzleFirstCube() {
        if (!(this.level instanceof ServerWorld)) return;

        Entity entity = ((ServerWorld) this.level).getEntity(this.entityUUIDs.get(0));

        if (entity instanceof TestElementEntity) {
            ((TestElementEntity) entity).startFizzling();
        } else if (entity != null && entity.isAlive()) {
            entity.remove();
        }

        this.entityUUIDs.remove(0);
    }

    // Close dropper and spawn a new cube inside the dropper
    public void closeDropper(CubeDropperBlock dropperBlock) {
        if (this.level instanceof ServerWorld) {
            dropperBlock.setOpen(false, this.getBlockState(), this.level, this.getBlockPos());
            this.openTicks = 0;
            addEntity();
        }
    }

    public void resetDropper() {
        if (this.entityUUIDs.size() == 2) {
            fizzleFirstCube();
        }
    }

    public void updateEntities() {
        if (this.entityUUIDs.isEmpty()) {
            addEntity();
        }
        if (this.level instanceof ServerWorld) {
            this.entityUUIDs.removeIf(uuid -> ((ServerWorld) this.level).getEntity(uuid) == null);
        }
    }

    public void addEntity() {
        if (this.entityNBT == null || !(this.level instanceof ServerWorld)) {
            return;
        }

        // Create new entity using nbt
        Entity entity = EntityType.loadEntityRecursive(this.entityNBT, this.level, e -> {
            e.moveTo(new Vec3(this.getBlockPos().south().east()).to3d());
            e.yRot = (ModUtil.symmetricRandom(15) + e.rotate(Rotation.getRandom(new Random())));
            e.setYBodyRot(e.yRot);
            e.setYHeadRot(e.yRot);
            return e;
        });

        if (entity == null) {
            return;
        }

        if (entity instanceof TestElementEntity) {
            ((TestElementEntity) entity).setFromDropper(true);
        }

        boolean successful = ((ServerWorld) this.level).tryAddFreshEntityWithPassengers(entity);

        if (successful) {
            this.entityUUIDs.add(entity.getUUID());
        }
    }

    public void removeAllEntities() {
        if (this.level instanceof ServerWorld) {
            for (UUID uuid : this.entityUUIDs) {
                Entity entity = ((ServerWorld) this.level).getEntity(uuid);
                if (entity != null) {
                    if (entity instanceof TestElementEntity) {
                        ((TestElementEntity) entity).startFizzling();
                    } else if (entity.isAlive()) {
                        entity.remove();
                    }
                }
            }
            this.entityUUIDs.clear();
        }
    }

    public void onEggClick(ItemStack egg, PlayerEntity player) {
        EntityType<?> spawnEggType = ((SpawnEggItem) egg.getItem()).getType(egg.getTag());

        if (!player.isCreative()) {
            egg.shrink(1);
            this.getEntityType().ifPresent(type -> player.addItem(new ItemStack(ForgeSpawnEggItem.fromEntityType(type))));
        }

        this.removeAllEntities();
        this.setEntityNBT(spawnEggType);
    }

    public void onWrenchClick(PlayerEntity player) {
        if (!this.hasEntityNBT()) return;

        // Remove current spawned cube
        if (this.entityUUIDs.size() > 1) {
            this.resetDropper();
        } else {
            // Clear entity type of dropper
            this.removeAllEntities();
            if (!player.isCreative()) {
                this.dropEgg();
            }

            this.removeEntityNBT();
        }
    }

    public void onRemove() {
        this.removeAllEntities();
        this.dropEgg();
    }

    public void dropEgg() {
        if (this.level == null) return;

        this.getEntityType().ifPresent(type -> {
            BlockPos blockPos = this.getBlockPos();
            ItemEntity entity = new ItemEntity(this.level,
                    blockPos.getX() + 1,
                    blockPos.getY() - 1.4,
                    blockPos.getZ() + 1,
                    new ItemStack(ForgeSpawnEggItem.fromEntityType(type)));
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, .5, 1));
            this.level.addFreshEntity(entity);
        });
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, World world, BlockPos pos) {
        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(pos));
        possibleIndicatorPositions.addAll(getSurroundingPositions(pos.below()));
        return possibleIndicatorPositions;
    }

    /*

    5      6       7      8
    4   entity   dropper  9
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

    public void setEntityNBT(EntityType<?> entityType) {
        this.entityNBT = new CompoundNBT();
        this.entityNBT.putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
    }

    public boolean hasEntityNBT() {
        return entityNBT != null;
    }

    public void removeEntityNBT() {
        this.entityNBT = null;
    }

    public Optional<EntityType<?>> getEntityType() {
        return this.entityNBT == null ? Optional.empty() : EntityType.by(this.entityNBT);
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
        nbt.putBoolean("Powered", this.wasAntlinePowered);
        nbt.putInt("OpenTicks", this.openTicks);

        if (this.entityNBT != null) {
            nbt.put("Entity", this.entityNBT);
        }

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
        this.wasAntlinePowered = nbt.getBoolean("Powered");
        this.openTicks = nbt.getInt("OpenTicks");

        this.entityNBT = null;
        if (nbt.contains("Entity")) {
            this.entityNBT = nbt.getCompound("Entity");
        }
    }
}
