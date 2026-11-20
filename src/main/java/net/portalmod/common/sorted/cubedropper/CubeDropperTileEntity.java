package net.portalmod.common.sorted.cubedropper;

import net.minecraft.world.item.SpawnEggItem;

import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;

import java.util.*;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class CubeDropperTileEntity extends BlockEntity implements IndicatorActivated {

    public List<UUID> entityUUIDs = new ArrayList<>();
    public int openTicks = 0;
    public boolean wasActive = false;
    public CompoundTag entityNBT = null;

    public CubeDropperTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityTypeInit.CUBE_DROPPER.get(), pos, state);
    }

    public void tick() {
        BlockState blockState = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        CubeDropperBlock dropperBlock = (CubeDropperBlock) blockState.getBlock();
        IndicatorInfo indicatorInfo = this.checkIndicators(blockState, this.level, pos);
        boolean active = !indicatorInfo.hasIndicators || indicatorInfo.allIndicatorsActivated;

        if (this.openTicks > 0) this.openTicks++;

        if (blockState.getBlock() instanceof CubeDropperBlock && this.entityNBT != null) {
            this.updateEntities();

            if (active && this.openTicks == 0 && (this.entityUUIDs.size() == 1 || !this.wasActive)) {
                openDropper(dropperBlock);
            }
        }

        this.wasActive = active;

        if (this.openTicks > 20) {
            closeDropper(dropperBlock);
        }
    }

    // Open dropper and fizzle first cube
    public void openDropper(CubeDropperBlock dropperBlock) {
        if (this.level instanceof ServerLevel) {
            dropperBlock.setOpen(true, this.getBlockState(), this.level, this.getBlockPos());
            this.openTicks = 1;
            if (this.entityUUIDs.size() == 2) {
                fizzleFirstCube();
            }
        }
    }

    public void fizzleFirstCube() {
        if (!(this.level instanceof ServerLevel)) return;

        Entity entity = ((ServerLevel) this.level).getEntity(this.entityUUIDs.get(0));

        if (entity instanceof TestElementEntity) {
            ((TestElementEntity) entity).startFizzling();
        } else if (entity != null && entity.isAlive()) {
            entity.remove();
        }

        this.entityUUIDs.remove(0);
    }

    // Close dropper and spawn a new cube inside the dropper
    public void closeDropper(CubeDropperBlock dropperBlock) {
        if (this.level instanceof ServerLevel) {
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
        if (this.entityUUIDs.isEmpty() && this.openTicks == 0) {
            addEntity();
        }
        if (this.level instanceof ServerLevel) {
            this.entityUUIDs.removeIf(uuid -> ((ServerLevel) this.level).getEntity(uuid) == null);
        }
    }

    public void addEntity() {
        if (this.entityNBT == null || !(this.level instanceof ServerLevel)) {
            return;
        }

        // Create new entity using nbt
        Entity entity = EntityType.loadEntityRecursive(this.entityNBT, this.level, e -> {
            e.moveTo(new Vec3(this.getBlockPos().south().east()).to3d());
            e.setYRot((ModUtil.symmetricRandom(15) + e.rotate(Rotation.getRandom(new Random()))));
            e.setYBodyRot(e.getYRot());
            e.setYHeadRot(e.getYRot());
            return e;
        });

        if (entity == null) {
            return;
        }

        if (entity instanceof TestElementEntity) {
            ((TestElementEntity) entity).setFromDropper(true);
        }

        boolean successful = ((ServerLevel) this.level).tryAddFreshEntityWithPassengers(entity);

        if (successful) {
            this.entityUUIDs.add(entity.getUUID());
        }
    }

    public void removeAllEntities() {
        if (this.level instanceof ServerLevel) {
            for (UUID uuid : this.entityUUIDs) {
                Entity entity = ((ServerLevel) this.level).getEntity(uuid);
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

    public void onEggClick(ItemStack egg, Player player) {
        EntityType<?> spawnEggType = ((SpawnEggItem) egg.getItem()).getType(egg.getTag());

        if (!player.isCreative()) {
            egg.shrink(1);
            this.getEntityType().ifPresent(type -> player.addItem(new ItemStack(SpawnEggItem.byId(type))));
        }

        this.removeAllEntities();
        this.setEntityNBT(spawnEggType);
    }

    public void onWrenchClick(Player player) {
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
                    new ItemStack(SpawnEggItem.byId(type)));
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, .5, 1));
            this.level.addFreshEntity(entity);
        });
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, Level world, BlockPos pos) {
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
        this.entityNBT = new CompoundTag();
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
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (!this.entityUUIDs.isEmpty()) {
            nbt.putUUID("UUID1", this.entityUUIDs.get(0));
        }
        if (this.entityUUIDs.size() >= 2) {
            nbt.putUUID("UUID2", this.entityUUIDs.get(1));
        }
        nbt.putBoolean("Powered", this.wasActive);
        nbt.putInt("OpenTicks", this.openTicks);

        if (this.entityNBT != null) {
            nbt.put("Entity", this.entityNBT);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.entityUUIDs.clear();
        if (nbt.hasUUID("UUID1")) {
            this.entityUUIDs.add(nbt.getUUID("UUID1"));
        }
        if (nbt.hasUUID("UUID2")) {
            this.entityUUIDs.add(nbt.getUUID("UUID2"));
        }
        this.wasActive = nbt.getBoolean("Powered");
        this.openTicks = nbt.getInt("OpenTicks");

        this.entityNBT = null;
        if (nbt.contains("Entity")) {
            this.entityNBT = nbt.getCompound("Entity");
        }
    }
}
