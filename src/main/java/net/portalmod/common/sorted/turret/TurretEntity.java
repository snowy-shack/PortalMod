package net.portalmod.common.sorted.turret;

import net.portalmod.core.init.DamageTypeInit;

import net.minecraft.network.chat.Component;
import com.mojang.datafixers.util.Pair;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.particles.TurretSparkParticle;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.core.init.*;
import net.portalmod.core.util.ModUtil;

import java.util.*;
import java.util.function.Predicate;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class TurretEntity extends TestElementEntity {

    public static final EntityDataAccessor<Integer> AMMO_ID = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> INFINITE_AMMO_ID = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> STATE_ID = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> ANIMATION_TICKS_ID = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> TIP_DIRECTION_RIGHT_ID = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int AMMO_PER_BULLET = 20;
    public static final int MAX_BULLETS = 64;
    public static final float BULLET_DAMAGE = 0.5f;
    public static final float BULLET_KNOCKBACK = 0.1f;

    public static final EntityDimensions DIMENSIONS = EntityDimensions.scalable(.5f, 1.2f);
    public static final EntityDimensions DEAD_DIMENSIONS = EntityDimensions.scalable(.5f, .5f);

    public static Predicate<LivingEntity> TARGETS = e ->
            !(e instanceof TestElementEntity)
            && !e.isSpectator()
            && !(e instanceof Player && ((Player) e).isCreative())
            && !(e.isBaby() && e.getClassification(false) != MobCategory.MONSTER)
            && !e.isInvisible()
            && !WrenchItem.holdingWrench(e);

    public int fallDuration = 10;
    public int viewDistance = 32;

    public LivingEntity previousTargetEntity = null;
    private int thisTargetShootingTicks = 0;

    public LivingEntity targetEntity = null;
    public Vec3 lastLaserPos = Vec3.ZERO;
    public Vec3 turretToTarget = Vec3.ZERO;

    public static DamageSource damageSource(LivingEntity entity) {
        return DamageTypeInit.source(entity.level(), DamageTypeInit.TURRET, entity);
    }
    public TurretEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public TurretEntity(Level level) {
        super(EntityInit.TURRET.get(), level);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return .75f;
    }

    @Override
    public float getEyeHeight(Pose pose) {
        return this.getState() == TurretState.DEAD ? 0.3f : .75f;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.getState() == TurretState.DEAD ? DEAD_DIMENSIONS : DIMENSIONS;
    }

    @Override
    public void tick() {
        boolean serverSide = !this.level().isClientSide;

        super.tick();

        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();
        if (this.isFizzling()) return;

        // If it's being held with a Portal Gun
        if (this.getVehicle() instanceof Player) {
            if (serverSide) {
                this.setState(TurretState.LOST_TARGET);
            }
            return;
        }

        this.animate();

        // Tipping over
        Vec3 deltaMovement = getDeltaMovement();
        Vec3 motionXZ = new Vec3(deltaMovement.x, 0, deltaMovement.z);
        if (motionXZ.length() > 0.01 && this.getState().isStanding() && this.onGround()) {
            if (serverSide) {
                this.setState(TurretState.FALLING);
                this.setTipDirectionRight(this.getLookAngle().yRot(90).dot(deltaMovement) < 0);
            }
            return;
        }

        this.updateTargetEntity();

        if (getState() == TurretState.SHOOTING || getState() == TurretState.FALLING) {
            this.shoot();
        } else {
            this.thisTargetShootingTicks = 0;
        }

        this.previousTargetEntity = this.targetEntity;

        if (this.tickCount == 1) {
            this.refreshDimensions();
        }
    }

    @Override
    public void fizzleTick() {
        super.fizzleTick();
        // TODO handle weird rotation of wings when fizzled (make it look in the direction it is looking)
    }

    public void animate() {
        this.setAnimationTicks(this.getAnimationTicks() + 1);

        if (getState() == TurretState.OPENING && this.getAnimationTicks() >= 10
                || getState() == TurretState.LOST_TARGET && this.getAnimationTicks() >= 20
                || getState() == TurretState.CLOSING && this.getAnimationTicks() >= 15
        ) {
            this.animationFinished();
            this.setAnimationTicks(0);
        }

        if (getState() == TurretState.RESTING || getState() == TurretState.SHOOTING) {
            this.setAnimationTicks(0);
        }

        if (getState() == TurretState.FALLING && this.getAnimationTicks() >= this.fallDuration) {
            this.setState(TurretState.DEAD); // I don't hate you.
        }
    }

    public boolean shouldShoot() {
        return this.targetEntity != null && this.turretView(targetEntity) != HitType.TRANSPARENT;
    }

    public void shoot() {
        if (!this.shouldShoot()) {
            return;
        }

        // "This is awkward" advancement
        if (this.targetEntity == this.previousTargetEntity && this.canShoot()) {
            this.thisTargetShootingTicks++;
            if (thisTargetShootingTicks > 200 && targetEntity instanceof ServerPlayer) {
                CriteriaTriggerInit.SURVIVE_TURRET.get().trigger((ServerPlayer) targetEntity);
            }
        } else {
            this.thisTargetShootingTicks = 0;
        }

        long tickTime = this.tickCount + this.getId();

        // Particles
        if (this.canShoot() && tickTime % 2 == 0 && this.level().isClientSide) {
            TurretSparkParticle.createGlowParticles(this.level(), this, this.turretToTarget);
        }

        // 4 shots fired per sound
        if (tickTime % 8 == 0) {
            this.playSound(this.canShoot() ? SoundInit.TURRET_FIRE.get() : SoundInit.TURRET_FIRE_FAIL.get(), 4.5f, ModUtil.randomSlightSoundPitch());
        }

        // Shoot every 2 ticks (10 times per second)
        if (tickTime % 2 != 0 || !canShoot()) {
            return;
        }

        if (!this.getInfiniteAmmo()) {
            this.setAmmo(this.getAmmo() - 1);
        }

        Vec3 you = this.getEyePosition(1.0F);
        Vec3 theGuySheTellsYouNotToWorryAbout = targetEntity.getEyePosition(1.0F).subtract(0F, 0.5F, 0F);

        AABB eyeToEye = new AABB(you, theGuySheTellsYouNotToWorryAbout);

        // Cube shield
        for (Cube cube : this.level().getNearbyEntities(Cube.class, TargetingConditions.DEFAULT, this, eyeToEye)) {
            AABB cubeAABB = cube.getBoundingBox();
            if (cubeAABB.clip(you, theGuySheTellsYouNotToWorryAbout).isPresent()) {
                cube.playSound(SoundInit.CUBE_HIT.get(), 0.75f, ModUtil.randomSoundPitch()); // A cube intercepted the bullets
                return;
            }
        }

        // Do damage only sometimes
        if (new Random().nextFloat() < 0.6f && !targetEntity.isBlocking()) {
            this.targetEntity.invulnerableTime = 0; // No mercy
            boolean hurt = this.targetEntity.hurt(damageSource(this), BULLET_DAMAGE);
            if (hurt) {
                Vec3 knockbackDirection = this.position().subtract(this.targetEntity.position()).scale(0.5);
                this.targetEntity.knockback(BULLET_KNOCKBACK, knockbackDirection.x, knockbackDirection.z);
            }
        }

        // Friendly Fire advancement
        if (targetEntity instanceof Monster) {
            Player nearestPlayer = level().getNearestPlayer(this, 10);
            if (nearestPlayer instanceof ServerPlayer) {
                CriteriaTriggerInit.TURRET_DEFENSE.get().trigger((ServerPlayer) nearestPlayer);
            }
        }

    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.isProjectile()) {
            if (this.getState().isStanding()) {
                this.setState(TurretState.FALLING);
                this.setTipDirectionRight(new Random().nextBoolean());
            }
            return false;
        }

        return super.hurt(source, damage);
    }

    public boolean canShoot() {
        return this.getAmmo() > 0 || this.getInfiniteAmmo();
    }

    public Pair<HitType, Vec3> traceAsFarAsPossible(Vec3 startPos, Vec3 endPos) {
        Vec3 direction = endPos.subtract(startPos).normalize();

        ClipContext context;
        BlockHitResult rayTraceResult;
        Vec3 transparentBlockPos = null;

        HitType hitType = HitType.CLEAR;
        while (true) {
            context = new ClipContext(startPos, endPos,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
            rayTraceResult = this.level().clip(context);

            if (rayTraceResult.getType() == HitResult.Type.MISS || startPos.distanceToSqr(endPos) <= 0.1) {
                return new Pair<>(hitType, transparentBlockPos != null ? transparentBlockPos : endPos);
            }

            BlockState blockState = this.level().getBlockState(rayTraceResult.getBlockPos());
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
        Vec3 turretPos = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 targetPos = new Vec3(targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ());

        if (targetEntity.level() != this.level() || targetPos.distanceToSqr(turretPos) > this.viewDistance * this.viewDistance) return HitType.SOLID;

        return this.traceAsFarAsPossible(turretPos, targetPos).getFirst();
    }

    public void updateTargetEntity() {
        AABB searchBox = new AABB(
                this.position().x - this.viewDistance, this.position().y - this.viewDistance, this.position().z - this.viewDistance,
                this.position().x + this.viewDistance, this.position().y + this.viewDistance, this.position().z + this.viewDistance
        );

        // Map entities to their distances
        Map<LivingEntity, Double> entityDistances = new HashMap<>();
        for (LivingEntity entity : this.level().getNearbyEntities(LivingEntity.class, new TargetingConditions().selector(TARGETS), this, searchBox)) {
            Vec3 ray = entity.position().subtract(this.position());
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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    public void onSpawnedByPlayer(Player player) {
        if (player.isCreative()) {
            this.setInfiniteAmmo(true);
            player.displayClientMessage(Component.translatable("actionbar.portalmod.turret.infinite_ammo"), true);
            //TODO: add particles (happy_villager probably)
        }

        this.setYRot(player.isShiftKeyDown() ? player.getYRot() : Math.round(player.getYRot() / 45) * 45);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot type) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot type, ItemStack itemStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int p_213333_2_, boolean p_213333_3_) {
        Entity entity = source.getEntity();
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return;
        }
        this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get(), this.getAmmo() / AMMO_PER_BULLET));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack holdingItem = player.getItemInHand(hand);
        if (holdingItem.getItem() == ItemInit.BULLETS.get()) {

            if (this.getAmmo() >= MAX_BULLETS * AMMO_PER_BULLET) {
                player.displayClientMessage(Component.translatable("actionbar.portalmod.turret.full"), true);
                return InteractionResult.PASS;
            }

            if (this.getInfiniteAmmo()) {
                player.displayClientMessage(Component.translatable("actionbar.portalmod.turret.infinite_ammo"), true);
                return InteractionResult.PASS;
            }

            int bulletStoreAmount = player.isShiftKeyDown() ? Math.min(holdingItem.getCount(), MAX_BULLETS - (this.getAmmo() / AMMO_PER_BULLET)) : 1;
            this.setAmmo(this.getAmmo() + bulletStoreAmount * AMMO_PER_BULLET);
            if (!player.isCreative()) {
                holdingItem.shrink(bulletStoreAmount);
            }

            if (!this.level().isClientSide) {
                this.setWiggle(10);
                this.setHurtDir(-this.getHurtDir());
                this.playSound(SoundInit.TURRET_STOCK.get(), 1, ModUtil.randomSlightSoundPitch());
            }

            return InteractionResult.SUCCESS;
        }
        else if (holdingItem.getItem() instanceof WrenchItem) {

            if (this.getInfiniteAmmo()) {
                player.displayClientMessage(Component.translatable("actionbar.portalmod.turret.infinite_ammo"), true);
                return InteractionResult.PASS;
            }

            if (this.getAmmo() == 0) {
                player.displayClientMessage(Component.translatable("actionbar.portalmod.turret.empty"), true);
                return InteractionResult.CONSUME;
            }

            if (this.getAmmo() >= AMMO_PER_BULLET) {
                this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get(), this.getAmmo() / AMMO_PER_BULLET), 0.8f);
            } else {
                this.spawnAtLocation(new ItemStack(ItemInit.BULLETS.get()), 0.8f);
            }

            this.setAmmo(0);

            if (!this.level().isClientSide) {
                this.setWiggle(10);
                this.setHurtDir(-this.getHurtDir());
                this.playSound(SoundInit.TURRET_STOCK.get(), 1, ModUtil.randomSlightSoundPitch());
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AMMO_ID, 0);
        this.entityData.define(INFINITE_AMMO_ID, false);
        this.entityData.define(STATE_ID, String.valueOf(TurretState.RESTING));
        this.entityData.define(ANIMATION_TICKS_ID, 0);
        this.entityData.define(TIP_DIRECTION_RIGHT_ID, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Ammo", this.getAmmo());
        nbt.putBoolean("InfiniteAmmo", this.getInfiniteAmmo());
        nbt.putString("State", String.valueOf(this.getState()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
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
        TurretState oldState = this.getState();

        this.entityData.set(STATE_ID, String.valueOf(state));

        this.setAnimationTicks(0);
        this.refreshDimensions();

        if (state == TurretState.OPENING || state == TurretState.FALLING && !oldState.wingsOpen()) {
            this.playSound(SoundInit.TURRET_OPEN.get(), 3.5f, ModUtil.randomSlightSoundPitch());
        } else if (state == TurretState.CLOSING || state == TurretState.DEAD && oldState.wingsOpen()) {
            this.playSound(SoundInit.TURRET_CLOSE.get(), 3.5f, ModUtil.randomSlightSoundPitch());
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

    public int getAnimationTicks() {
        return this.entityData.get(ANIMATION_TICKS_ID);
    }

    public void setAnimationTicks(int animationTick) {
        this.entityData.set(ANIMATION_TICKS_ID, animationTick);
    }

    public boolean getTipDirectionRight() {
        return this.entityData.get(TIP_DIRECTION_RIGHT_ID);
    }

    public void setTipDirectionRight(boolean right) {
        this.entityData.set(TIP_DIRECTION_RIGHT_ID, right);
    }
}