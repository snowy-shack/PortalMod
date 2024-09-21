package net.portalmod.common.sorted.turret;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import java.util.*;

public class TurretEntity extends TestElementEntity {

    public static final DataParameter<Integer> AMMO_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> INFINITE_AMMO_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.BOOLEAN);
    public static final int AMMO_PER_BULLET = 20;
    public static final int MAX_BULLETS = 64;
    public static final DamageSource TURRET_DAMAGE_SOURCE = new DamageSource("turret");
    public static final float TURRET_DAMAGE = 0.01f;

    public TurretState state = TurretState.RESTING;
    public LivingEntity targetEntity = null;
    public Vector3d lastLaserPos = Vector3d.ZERO;
    public int animationTick = 0;

    public TurretEntity(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    public TurretEntity(World level) {
        super(EntityInit.TURRET.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        this.updateTargetEntity();

        this.animate();

        if (this.state == TurretState.SHOOTING) {
            this.shoot();
        }

        this.yBodyRot = this.yRot;
    }

    public void animate() {
        if (this.state == TurretState.OPENING && this.animationTick >= 10
                || this.state == TurretState.LOST_TARGET && this.animationTick >= 20
                || this.state == TurretState.CLOSING && this.animationTick >= 15
        ) {
            this.animationFinished();
            this.animationTick = 0;
        }

        if (this.state == TurretState.RESTING || this.state == TurretState.SHOOTING) {
            this.animationTick = 0;
        }

        this.animationTick++;
    }

    public void shoot() {
        if (this.targetEntity == null || this.getAmmo() == 0 && !this.getInfiniteAmmo()) {
            return;
        }

        // Shoot every 5 ticks (4 times per second)
        if (this.level.getGameTime() % 5 != 0) {
            return;
        }

        // Do damage
        if (new Random().nextFloat() < 0.8f) {
            boolean hurt = this.targetEntity.hurt(TURRET_DAMAGE_SOURCE, TURRET_DAMAGE);
            if (hurt) {
                Vector3d knockback = this.position().subtract(this.targetEntity.position());
                this.targetEntity.knockback(0.4f, knockback.x, knockback.z);
            }
        }

        this.playSound(SoundInit.TURRET_FIRE.get(), 4.5f, 1);

        this.setAmmo(this.getAmmo() - 1);
    }

    public void updateTargetEntity() {
        // Map players to their distances
        Map<PlayerEntity, Double> playerDistances = new HashMap<>();
        for (PlayerEntity player : this.level.players()) {
            Vector3d ray = player.position().subtract(this.position());
            double cosine = ray.normalize().dot(this.getLookAngle());
            double distanceSqr = ray.lengthSqr();
            // In range and in front of turret (cone shape)
            if (distanceSqr < 1225 && cosine > 0.6 && !player.isCreative() && !player.isSpectator()) {
                playerDistances.put(player, distanceSqr);
            }
        }

        // Sort players
        List<PlayerEntity> orderedPlayers = new ArrayList<>();
        playerDistances.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry::getValue)).forEach(
                entry -> orderedPlayers.add(entry.getKey())
        );

        for (PlayerEntity player : orderedPlayers) {
            if (this.canSee(player)) {
                if (player != this.targetEntity) {
                    this.targetAcquired(player);   // Update if not already target
                }
                return;
            }
        }

        if (this.targetEntity != null) {
            this.targetLost();   // Update if not already null
        }
    }

    public void targetAcquired(LivingEntity entity) {
        this.targetEntity = entity;

        if (this.state == TurretState.RESTING) {
            this.setState(TurretState.OPENING);
        }
    }

    public void targetLost() {
        this.targetEntity = null;

        if (this.state == TurretState.SHOOTING) {
            this.setState(TurretState.LOST_TARGET);
        }
    }

    public void animationFinished() {
        switch (this.state) {
            case OPENING:
            case LOST_TARGET:
                this.setState(this.targetEntity == null ? TurretState.CLOSING : TurretState.SHOOTING);
                break;
            case CLOSING:
                this.setState(this.targetEntity == null ? TurretState.RESTING : TurretState.OPENING);
                break;
        }
    }

    public boolean hasTarget() {
        return this.targetEntity != null;
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
                this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get(), this.getAmmo() / AMMO_PER_BULLET), 0.8f);
            } else {
                this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get()), 0.8f);
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

    public void setState(TurretState state) {
        if (this.state != state) {
            this.state = state;

            if (state == TurretState.OPENING) {
                this.playSound(SoundInit.TURRET_DEPLOY.get(), 3.5f, 1);
            } else if (state == TurretState.CLOSING) {
                this.playSound(SoundInit.TURRET_RETRACT.get(), 3.5f, 1);
            }

            if (this.level.isClientSide()) {
                ModUtil.sendChatMessage(level, this.state.name());
            }
        }
    }

    public boolean shouldLaserMove() {
        return this.state == TurretState.SHOOTING;
    }

    public boolean shouldLaserEase() {
        return this.state == TurretState.SHOOTING || this.state == TurretState.LOST_TARGET;
    }
}