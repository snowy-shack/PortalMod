package net.portalmod.common.sorted.turret;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.util.ModUtil;

import java.util.Collections;

public class TurretEntity extends TestElementEntity {

    public static final DataParameter<Integer> AMMO_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> INFINITE_AMMO_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.BOOLEAN);
    public static final int AMMO_PER_BULLET = 20;
    public static final int MAX_BULLETS = 64;

    public TurretEntity(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    public TurretEntity(World level) {
        super(EntityInit.TURRET.get(), level);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes();
    }

    public void onSpawnedByPlayer(PlayerEntity player) {
        if (player.isCreative()) {
            this.setInfiniteAmmo(true);
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.turret.infinite_ammo"), true);
            //TODO: add particles (happy_villager probably)
        }

        this.yRot = player.isShiftKeyDown() ? player.yRot : Math.round(player.yRot / 45) * 45;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlotType type) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlotType type, ItemStack itemStack) {

    }

    @Override
    public HandSide getMainArm() {
        return HandSide.RIGHT;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int p_213333_2_, boolean p_213333_3_) {
        Entity entity = source.getEntity();
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
            return;
        }
        this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get(), this.getAmmo() / AMMO_PER_BULLET));
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        ItemStack holdingItem = player.getItemInHand(hand);
        if (holdingItem.getItem() == ItemInit.BULLETS.get()) {

            if (this.getAmmo() >= MAX_BULLETS * AMMO_PER_BULLET) {
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.turret.full"), true);
                return ActionResultType.PASS;
            }

            if (this.getInfiniteAmmo()) {
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.turret.infinite_ammo"), true);
                return ActionResultType.PASS;
            }

            int bulletStoreAmount = player.isShiftKeyDown() ? Math.min(holdingItem.getCount(), MAX_BULLETS - (this.getAmmo() / AMMO_PER_BULLET)) : 1;
            this.setAmmo(this.getAmmo() + bulletStoreAmount * AMMO_PER_BULLET);
            if (!player.isCreative()) {
                holdingItem.shrink(bulletStoreAmount);
            }

            if (!this.level.isClientSide) {
                this.setWiggle(10);
                this.setHurtDir(-this.getHurtDir());
                this.playSound(SoundEvents.ARMOR_EQUIP_CHAIN, 1, 1);
            }

            return ActionResultType.SUCCESS;
        }
        else if (holdingItem.getItem() instanceof WrenchItem) {

            if (this.getAmmo() == 0) {
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.turret.empty"), true);
                return ActionResultType.CONSUME;
            }

            if (this.getAmmo() >= AMMO_PER_BULLET) {
                this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get(), this.getAmmo() / AMMO_PER_BULLET));
            } else {
                this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get()));
            }

            this.setAmmo(0);

            if (!this.level.isClientSide) {
                this.setWiggle(10);
                this.setHurtDir(-this.getHurtDir());
                this.playSound(SoundEvents.ARMOR_EQUIP_CHAIN, 1, 1);
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    @Override
    public void tick() {
        super.tick();

        this.yBodyRot = this.yRot;
    }

    // Copy of Cube.rideTick()
    @Override
    public void rideTick() {
        this.setDeltaMovement(Vector3d.ZERO);
        this.tick();

        if(this.getVehicle() instanceof PlayerEntity) {
            this.yRot = this.getVehicle().getYHeadRot();
            this.yBodyRot = this.yRot;
        } else {
            if(!(this.getVehicle() instanceof AbstractMinecartEntity
                    || this.getVehicle() instanceof BoatEntity))
                this.stopRiding();
            return;
        }

        PlayerEntity player = (PlayerEntity)getVehicle();
        if(!(player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof PortalGun)
                && !(player.getItemInHand(Hand.OFF_HAND).getItem() instanceof PortalGun)) {
            this.stopRiding();
            return;
        }

        final float factor = 2;
        Vector3d eyePos = player.getEyePosition(1).add(0, -.4, 0);

        float xRot = player.getViewXRot(1);
        float yRot = player.getViewYRot(1);

        xRot *= (float)Math.PI / 180f;
        yRot *= -(float)Math.PI / 180f;
        float cosy = MathHelper.cos(yRot);
        float siny = MathHelper.sin(yRot);
        float cosx = MathHelper.cos(xRot);
        float sinx = MathHelper.sin(xRot);
        float x = siny * cosx;
        float y = -sinx;
        float z = cosy * cosx;

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        Vector3d ridingPos = new Vector3d(
                eyePos.x + x * factor,
                eyePos.y + y * factor,
                eyePos.z + z * factor);

        this.move(MoverType.SELF, ridingPos.subtract(ModUtil.getOldPos(this)));

        if(this.position().distanceTo(ridingPos) > 1.5) {
            this.stopRiding();
        }

        this.fallDistance = 0;

        if (this.isFizzling()) {
            this.stopRiding();
        }
    }

    @Override
    public void stopRiding() {
        this.removeVehicle();
        this.boardingCooldown = 0;

        Vector3d momentum = this.position().subtract(ModUtil.getOldPos(this));
        this.setDeltaMovement(momentum);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AMMO_ID, 0);
        this.entityData.define(INFINITE_AMMO_ID, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Ammo", this.getAmmo());
        nbt.putBoolean("InfiniteAmmo", this.getInfiniteAmmo());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        this.setAmmo(nbt.getInt("Ammo"));
        this.setInfiniteAmmo(nbt.getBoolean("InfiniteAmmo"));
    }

    public int getAmmo() {
        return this.entityData.get(AMMO_ID);
    }

    public void setAmmo(int ammo) {
        this.entityData.set(AMMO_ID, ammo);
    }

    public boolean getInfiniteAmmo() {
        return this.entityData.get(INFINITE_AMMO_ID);
    }

    public void setInfiniteAmmo(boolean infiniteAmmo) {
        this.entityData.set(INFINITE_AMMO_ID, infiniteAmmo);
    }
}