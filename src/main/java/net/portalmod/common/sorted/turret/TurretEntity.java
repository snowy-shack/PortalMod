package net.portalmod.common.sorted.turret;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.particles.TurretSparkParticle;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.core.init.*;
import net.portalmod.core.util.ModUtil;

import java.util.*;
import java.util.function.Predicate;

public class TurretEntity extends TestElementEntity {

    public static final DataParameter<Integer> AMMO_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> INFINITE_AMMO_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<String> STATE_ID = EntityDataManager.defineId(TurretEntity.class, DataSerializers.STRING);
    public static final int AMMO_PER_BULLET = 20;
    public static final int MAX_BULLETS = 64;
    public static final float BULLET_DAMAGE = 0.5f;
    public static final float BULLET_KNOCKBACK = 0.1f;

    public static Predicate<LivingEntity> TARGETS = e ->
            !(e instanceof TestElementEntity)
            && !e.isSpectator()
            && !(e instanceof PlayerEntity && ((PlayerEntity) e).isCreative())
            && !(e.isBaby() && e.getClassification(false) != EntityClassification.MONSTER)
            && !e.isInvisible();

    public int fallDuration = 10;
    public int viewDistance = 32;

    public LivingEntity previousTargetEntity = null;
    private int thisTargetShootingTicks = 0;

    public LivingEntity targetEntity = null;
    public Vector3d lastLaserPos = Vector3d.ZERO;
    public Vector3d turretToTarget = Vector3d.ZERO;
    public Vector3d tipDirection = Vector3d.ZERO;
    public int animationTick = 0;

    public static DamageSource damageSource(LivingEntity entity) {
        return new EntityDamageSource("turret", entity);
    }
    public TurretEntity(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    public TurretEntity(World level) {
        super(EntityInit.TURRET.get(), level);
    }

    @Override
    protected float getStandingEyeHeight(Pose p_213348_1_, EntitySize p_213348_2_) {
        return 0.75F;
    }

    @Override
    public void tick() {
        super.tick();
        this.yBodyRot = this.yRot;
        if (this.isFizzling()) return;

        // If it's being held with a Portal Gun
        if (this.getVehicle() instanceof PlayerEntity) {
            setState(TurretState.LOST_TARGET);
            return;
        }

        this.animate();

        // Tipping over
        Vector3d motionXZ = new Vector3d(getDeltaMovement().x, 0, getDeltaMovement().z);
        if (motionXZ.length() > 0.1
                && getState() != TurretState.FALLING
                && getState() != TurretState.DEAD
                && this.onGround) {
            this.animationTick = 0;
            setState(TurretState.FALLING);
            this.tipDirection = motionXZ.normalize();
            return;
        }

        this.updateTargetEntity();

        if (getState() == TurretState.SHOOTING || getState() == TurretState.FALLING) {
            this.shoot();
        } else this.thisTargetShootingTicks = 0;

        this.previousTargetEntity = this.targetEntity;
    }

    @Override
    public void fizzleTick() {
        super.fizzleTick();
        // TODO handle weird rotation of wings when fizzled (make it look in the direction it is looking)
    }

    public void animate() {
        if (getState() == TurretState.OPENING && this.animationTick >= 10
                || getState() == TurretState.LOST_TARGET && this.animationTick >= 20
                || getState() == TurretState.CLOSING && this.animationTick >= 15
        ) {
            this.animationFinished();
            this.animationTick = 0;
        }

        if (getState() == TurretState.FALLING && this.animationTick >= this.fallDuration) {
            setState(TurretState.DEAD); // I don't hate you.
            this.animationTick = 0;
        }

        if (getState() == TurretState.RESTING || getState() == TurretState.SHOOTING) {
            this.animationTick = 0;
        }

        this.animationTick++;
    }

    public void shoot() {
        if (this.targetEntity == null || WrenchItem.holdingWrench(this.targetEntity) || this.turretView(targetEntity) == HitType.TRANSPARENT) {
            return;
        }

        // "This is awkward" advancement
        if (this.targetEntity == this.previousTargetEntity && this.canShoot()) {
            this.thisTargetShootingTicks++;
            if (thisTargetShootingTicks > 200 && targetEntity instanceof ServerPlayerEntity) {
                CriteriaTriggerInit.SURVIVE_TURRET.get().trigger((ServerPlayerEntity) targetEntity);
            }
        } else {
            this.thisTargetShootingTicks = 0;
        }

        // Particles
        if (this.canShoot() && this.level.getGameTime() % 2 == 0) {
            TurretSparkParticle.createGlowParticles(this.level, this, this.turretToTarget);
        }

        // Sounds
        if (this.level.getGameTime() % 8 == 0) {
            this.playSound(this.canShoot() ? SoundInit.TURRET_FIRE.get() : SoundInit.TURRET_FIRE_FAIL.get(), 4.5f, 1);
        }

        // Shoot every 4 ticks (5 times per second)
        if (this.level.getGameTime() % 2 != 0 || !canShoot()) {
            return;
        }

        if (!this.getInfiniteAmmo()) {
            this.setAmmo(this.getAmmo() - 1);
        }

        Vector3d you = this.getEyePosition(1.0F);
        Vector3d theGuySheTellsYouNotToWorryAbout = targetEntity.getEyePosition(1.0F).subtract(0F, 0.5F, 0F);

        AxisAlignedBB eyeToEye = new AxisAlignedBB(you, theGuySheTellsYouNotToWorryAbout);

        // Cube shield
        for (Cube cube : this.level.getNearbyEntities(Cube.class, EntityPredicate.DEFAULT, this, eyeToEye)) {
            AxisAlignedBB cubeAABB = cube.getBoundingBox();
            if (cubeAABB.clip(you, theGuySheTellsYouNotToWorryAbout).isPresent()) {
                cube.playSound(SoundInit.CUBE_HIT.get(), 0.75f, ModUtil.randomSoundPitch()); // A cube intercepted the bullets
                return;
            }
        }

        // Do damage
        if (new Random().nextFloat() < 0.6f && !targetEntity.isBlocking()) {
            this.targetEntity.invulnerableTime = 0; // No mercy
            boolean hurt = this.targetEntity.hurt(damageSource(this), BULLET_DAMAGE);
            if (hurt) {
                Vector3d knockbackDirection = this.position().subtract(this.targetEntity.position()).scale(0.5);
                this.targetEntity.knockback(BULLET_KNOCKBACK, knockbackDirection.x, knockbackDirection.z);
            }
        }

        // Friendly Fire advancement
        if (targetEntity instanceof MonsterEntity) {
            PlayerEntity nearestPlayer = level.getNearestPlayer(this, 10);
            if (nearestPlayer instanceof ServerPlayerEntity) {
                CriteriaTriggerInit.TURRET_DEFENSE.get().trigger((ServerPlayerEntity) nearestPlayer);
            }
        }

    }

    public boolean canShoot() {
        return this.getAmmo() > 0 || this.getInfiniteAmmo();
    }

    public Pair<HitType, Vector3d> traceAsFarAsPossible(Vector3d startPos, Vector3d endPos) {
        Vector3d direction = endPos.subtract(startPos).normalize();

        RayTraceContext context;
        BlockRayTraceResult rayTraceResult;
        Vector3d transparentBlockPos = null;

        HitType hitType = HitType.CLEAR;
        while (true) {
            context = new RayTraceContext(startPos, endPos,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this);
            rayTraceResult = this.level.clip(context);

            if (rayTraceResult.getType() == RayTraceResult.Type.MISS || startPos.distanceToSqr(endPos) <= 0.1) {
                return new Pair<>(hitType, transparentBlockPos != null ? transparentBlockPos : endPos);
            }

            BlockState blockState = this.level.getBlockState(rayTraceResult.getBlockPos());
            Block block = blockState.getBlock();

            if (hitType != HitType.TRANSPARENT && block.is(BlockTagInit.BLOCK_PERMEABLE)) hitType = HitType.PERMEABLE;
            if (block.is(BlockTagInit.BLOCK_TRANSPARENT) && transparentBlockPos == null) {
                transparentBlockPos = rayTraceResult.getLocation();
                hitType = HitType.TRANSPARENT;
            }

            if (!block.is(BlockTagInit.BLOCK_PERMEABLE) && !block.is(BlockTagInit.BLOCK_TRANSPARENT)) {
                return new Pair<>(HitType.SOLID, transparentBlockPos != null ? transparentBlockPos : rayTraceResult.getLocation());
            }

            startPos = rayTraceResult.getLocation().add(direction.scale(0.3));
        }
    }

    public HitType turretView(Entity targetEntity) {
        if (targetEntity == null) return HitType.SOLID;
        Vector3d turretPos = new Vector3d(this.getX(), this.getEyeY(), this.getZ());
        Vector3d targetPos = new Vector3d(targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ());

        if (targetEntity.level != this.level || targetPos.distanceToSqr(turretPos) > this.viewDistance * this.viewDistance) return HitType.SOLID;

        return this.traceAsFarAsPossible(turretPos, targetPos).getFirst();
    }

    public void updateTargetEntity() {
        AxisAlignedBB searchBox = new AxisAlignedBB(
                this.position().x - this.viewDistance, this.position().y - this.viewDistance, this.position().z - this.viewDistance,
                this.position().x + this.viewDistance, this.position().y + this.viewDistance, this.position().z + this.viewDistance
        );

        // Map entities to their distances
        Map<LivingEntity, Double> entityDistances = new HashMap<>();
        for (LivingEntity entity : this.level.getNearbyEntities(LivingEntity.class, new EntityPredicate().selector(TARGETS), this, searchBox)) {
            Vector3d ray = entity.position().subtract(this.position());
            double cosine = ray.normalize().dot(this.getLookAngle());
            double distanceSqr = ray.lengthSqr();

            // No shooting the same team
            if (this.getTeam() != null && entity.getTeam() != null && this.getTeam().isAlliedTo(entity.getTeam())) continue;

            // In front of turret (cone shape)
            if (cosine > 0.6) entityDistances.put(entity, distanceSqr);
        }

        // Sort entities
        ArrayList<Map.Entry<LivingEntity, Double>> orderedEntities = new ArrayList<>(entityDistances.entrySet());
        orderedEntities.sort(Map.Entry.comparingByValue());

        // Target closest entity
        for (Map.Entry<LivingEntity, Double> entry : orderedEntities) {
            LivingEntity entity = entry.getKey();
            if (this.turretView(entity) != HitType.SOLID) {
                if (entry != this.targetEntity) this.targetAcquired(entity);
                return;
            }
        }

        // No targets found
        if (this.targetEntity != null) {
            this.targetLost();
        }
    }

    public void targetAcquired(LivingEntity entity) {
        this.targetEntity = entity;

        if (getState() == TurretState.RESTING) {
            this.setState(TurretState.OPENING);
        }
    }

    public void targetLost() {
        this.targetEntity = null;

        if (getState() == TurretState.SHOOTING) {
            this.setState(TurretState.LOST_TARGET);
        }
    }

    public void animationFinished() {
        switch (getState()) {
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
                this.playSound(SoundInit.TURRET_STOCK.get(), 1, 1);
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
                this.playSound(SoundInit.TURRET_STOCK.get(), 1, 1);
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
        this.entityData.define(STATE_ID, String.valueOf(TurretState.RESTING));
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Ammo", this.getAmmo());
        nbt.putBoolean("InfiniteAmmo", this.getInfiniteAmmo());
        nbt.putString("State", String.valueOf(this.getState()));
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);

        if (nbt.contains("Ammo")) {
            this.setAmmo(nbt.getInt("Ammo"));
        }
        if (nbt.contains("InfiniteAmmo")) {
            this.setInfiniteAmmo(nbt.getBoolean("InfiniteAmmo"));
        }
        if (nbt.contains("State")) {
            this.setState(TurretState.valueOf(nbt.getString("State")));
        }
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
        if (getState() != state) {
            this.entityData.set(STATE_ID, String.valueOf(state));
            this.setState(state);

            if (state == TurretState.OPENING || state == TurretState.FALLING) {
                this.playSound(SoundInit.TURRET_OPEN.get(), 3.5f, 1);
            } else if (state == TurretState.CLOSING || state == TurretState.DEAD) {
                this.playSound(SoundInit.TURRET_CLOSE.get(), 3.5f, 1);
            }
        }
    }

    public TurretState getState() {
        return TurretState.valueOf(this.entityData.get(STATE_ID));
    }

    public boolean shouldLaserMove() {
        return getState() == TurretState.SHOOTING || getState() == TurretState.OPENING;
    }

    public boolean shouldLaserEase() {
        return getState() == TurretState.SHOOTING || getState() == TurretState.LOST_TARGET || getState() == TurretState.OPENING;
    }
}