package net.portalmod.common.entities;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.particles.FizzleFlakeParticle;
import net.portalmod.common.particles.FizzleGlowParticle;
import net.portalmod.common.particles.PortalGunSparkParticle;
import net.portalmod.common.sorted.fizzler.Fizzler;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunInteraction;
import net.portalmod.core.init.EntityTagInit;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Entity which can be fizzled, dropped by a cube dropper, picked up using a portal gun and broken with a wrench.
 */
public abstract class TestElementEntity extends LivingEntity {

    public static final DataParameter<Integer> FIZZLE_TICKS_ID = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> FROM_DROPPER_ID = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WIGGLE_ID = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> DATA_ID_HURT = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> DATA_ID_HURTDIR = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    private static final DataParameter<Float> DATA_ID_DAMAGE = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.FLOAT);

    public static final float HOLDING_DISTANCE = 1.5f;

    public int maxFizzleTime = 35;
    public boolean canFizzle;
    public Vector3d lastPos;

    public TestElementEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
        this.canFizzle = true;
    }

    @Override
    public void tick() {
        if (this.lastPos == null) {
            this.lastPos = this.position().scale(1);
        }

        if (this.getWiggle() > 0) {
            this.setWiggle(this.getWiggle() - 1);
        }

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        super.tick();

        if (this.isFizzling()) {
            this.fizzleTick();
        } else {
            this.checkTraversedBlocks();
        }

        this.lastPos = this.position().scale(1);
    }

    public void checkTraversedBlocks() {
        AxisAlignedBB movementBox = new AxisAlignedBB(this.lastPos, this.position());
        Stream<BlockPos> collidedPositions = BlockPos.betweenClosedStream(movementBox);
        collidedPositions.forEach(pos -> {
            BlockState state = this.level.getBlockState(pos);
            if (this.isInsideFizzler(pos, state, movementBox)) {
                this.startFizzling();
                this.fizzleTick();
            }
        });
    }

    private boolean isInsideFizzler(BlockPos pos, BlockState state, AxisAlignedBB box) {
        Block block = state.getBlock();
        if (block instanceof Fizzler) {
            return ((Fizzler) block).isInsideField(box, pos, state);
        }

        return false;
    }

    public boolean isFizzling() {
        return this.getFizzleTicks() > 0;
    }

    public void startFizzling() {
        if (canFizzle && !this.isFizzling()) {
            this.setFizzleTicks(this.getFizzleTicks() + 1);
        }
    }

    public int getFizzleLight(int packedLight) {
        int fizzleAmount = (int) (this.getFizzleTicks() * 0.6);
        return LightTexture.pack(
                Math.max(0, LightTexture.block(packedLight) - fizzleAmount),
                Math.max(0, LightTexture.sky(packedLight) - fizzleAmount)
        );
    }

    public void fizzleTick() {
        int fizzleTicks = this.getFizzleTicks();

        if (fizzleTicks == 1) {
            this.fizzleInit();
        }

        double minSpeed = 0.08;
        Vector3d xzMovement = this.getDeltaMovement().multiply(1, 0, 1);

        // If no horizontal movement, choose randomly
        if (xzMovement.length() == 0) {
            xzMovement = new Vector3d(new Random().nextFloat() * 0.1 - 0.05, 0, new Random().nextFloat() * 0.1 - 0.05);
        }

        Vector3d newMovement = xzMovement.length() < minSpeed ? xzMovement.normalize().scale(minSpeed) : xzMovement.scale(0.95);
        this.setDeltaMovement(newMovement);

        FizzleGlowParticle.createGlowParticles(level, this);
        FizzleFlakeParticle.createFlakeParticles(level, this);

        // Rotate slowly
        this.yRot += 35f * (float) newMovement.length();
        this.yHeadRot = this.yRot;

        if (fizzleTicks > this.maxFizzleTime && this.isAlive()) {
            this.fizzleKill();
        }

        this.setFizzleTicks(fizzleTicks + 1);
    }

    /**
     * On the first tick of fizzling
     */
    public void fizzleInit() {
        this.setNoGravity(true);
        this.level.playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.ENTITY_FIZZLE.get(), SoundCategory.NEUTRAL, 1, 1);

        Entity holder = this.getVehicle();

        if (this.isPassenger() && holder instanceof PlayerEntity) {
            ((PlayerEntity) holder).awardStat(Stats.ENTITY_KILLED.get(this.getType()));

            if (holder instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(
                        (ServerPlayerEntity) holder,
                        this,
                        DamageSource.playerAttack((PlayerEntity) holder)
                );
            }
        }
    }

    /**
     * On the last tick of fizzling
     */
    public void fizzleKill() {
        if (!this.isFromDropper() && !this.getType().is(EntityTagInit.FIZZLER_NO_ITEM_DROPS) && !this.level.isClientSide) {
            this.dropAllDeathLoot(new DamageSource("fizzle"));
        }
        this.remove();
    }

    public boolean pickUp(PlayerEntity player) {
        boolean riding = this.startRiding(player);
        if (!riding) return false;

        if (player.level.isClientSide()) {
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.PICK_ENTITY).data(this.getId()).build());
        }

        return true;
    }

    @Override
    public void rideTick() {
        this.setDeltaMovement(Vector3d.ZERO);
        this.tick();

        if (!(this.getVehicle() instanceof PlayerEntity)) {
            if (this.getVehicle() instanceof AbstractMinecartEntity || this.getVehicle() instanceof BoatEntity) {
                super.rideTick();
            } else {
                this.stopRiding();
            }
            return;
        }

        this.yRot = this.getVehicle().getYHeadRot();
        this.yBodyRot = this.yRot;

        PlayerEntity player = (PlayerEntity)getVehicle();
        Vector3d eyePos = player.getEyePosition(1).add(0, -0.4, 0);

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
                eyePos.x + x * HOLDING_DISTANCE,
                eyePos.y + y * HOLDING_DISTANCE,
                eyePos.z + z * HOLDING_DISTANCE);

        this.move(MoverType.SELF, ridingPos.subtract(ModUtil.getOldPos(this)));

        if(this.position().distanceTo(ridingPos) > 1) {
            dropHeldEntities(player, false, player.getMainHandItem());
        }

        this.fallDistance = 0;

        if (player.getMainHandItem().getItem() instanceof PortalGun) {
            PortalGunSparkParticle.createParticles(this.level, player, false);
        }
        if (player.getOffhandItem().getItem() instanceof PortalGun) {
            PortalGunSparkParticle.createParticles(this.level, player, true);
        }

        if (this.isFizzling()) {
            dropHeldEntities(player, false, player.getMainHandItem());
        }
    }

    @Override
    public void stopRiding() {
        this.removeVehicle();
        this.boardingCooldown = 0;

        Vector3d momentum = this.position().subtract(ModUtil.getOldPos(this));
        this.setDeltaMovement(momentum);
    }

    public static void dropHeldEntities(PlayerEntity player, boolean yeet, ItemStack itemStack) {
        List<Entity> passengers = player.getPassengers();
        for (int i = passengers.size() - 1; i >= 0; --i) {
            Entity entity = passengers.get(0);
            if (!(entity instanceof TestElementEntity)) {
                continue;
            }

            entity.stopRiding();

            if (itemStack.getItem() instanceof PortalGun) {
                PortalGun.dropCube(player, itemStack);
            }

            float maxSpeed = 0.5f;

            boolean exceedsLimit = entity.getDeltaMovement().add(player.getDeltaMovement().reverse()).length() > maxSpeed;
            if (exceedsLimit) entity.setDeltaMovement(entity.getDeltaMovement().normalize().multiply(maxSpeed, maxSpeed, maxSpeed).add(player.getDeltaMovement()));

            if (yeet) {
                float strength = .3f;
                entity.setDeltaMovement(entity.getDeltaMovement().add(player.getViewVector(0)
                        .multiply(strength, strength, strength)));
            }
        }
    }

    public boolean isHoldable() {
        return !this.isFizzling();
    }

    public static boolean isHoldable(Entity entity) {
        return entity instanceof TestElementEntity && ((TestElementEntity) entity).isHoldable();
    }

    public boolean isPickedUp() {
        return this.getVehicle() != null && this.getVehicle() instanceof PlayerEntity;
    }

    public void dropIfPickedUp() {
        if (this.isPickedUp()) {
            PlayerEntity player = (PlayerEntity) this.getVehicle();
            assert player != null;
            dropHeldEntities(player, false, player.getMainHandItem());
        }
    }

    @Override
    public void remove(boolean keepData) {
        super.remove(keepData);
        this.dropIfPickedUp();
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.level.isClientSide || !this.isAlive()) {
            return false;
        }

        boolean shouldSwing = true;
        boolean holdingWrench = source instanceof EntityDamageSource && source.getEntity() instanceof LivingEntity && WrenchItem.hitWithWrench((LivingEntity) source.getEntity());
        boolean outOfWorld = source == DamageSource.OUT_OF_WORLD;
        boolean inGoo = source == FluidInit.GOO_DAMAGE && this.getVehicle() == null;
        boolean isCreative = source instanceof EntityDamageSource && source.getEntity() instanceof PlayerEntity && ((PlayerEntity) source.getEntity()).isCreative();

        boolean shouldHurt = holdingWrench || inGoo || outOfWorld || isCreative;

        if (holdingWrench) {
            damage *= 1.5f;
//            this.playSound(SoundInit.CUBE_HIT.get(), 0.75f, ModUtil.randomSoundPitch());
        }

        if (inGoo) {
            damage *= 0.1f;
            shouldSwing = false;
        }

        if (shouldHurt) {
            this.applyDamage(source, damage, shouldSwing, isCreative);
            return true;
        }

        return false;
    }

    public void applyDamage(DamageSource source, float damage, boolean swing, boolean creative) {
        if (swing) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
        }
        this.setDamage(this.getDamage() + damage * 10.0F);
        this.markHurt();
        if (creative || this.getDamage() > 40.0F) {
            this.remove();
            if (!creative && !this.isFromDropper() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                this.dropAllDeathLoot(source);
            }
        }
    }

    @Override
    public void animateHurt() {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FIZZLE_TICKS_ID, 0);
        this.entityData.define(FROM_DROPPER_ID, false);
        this.entityData.define(WIGGLE_ID, 0);
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, 0.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("FizzleTicks", this.getFizzleTicks());
        nbt.putBoolean("FromDropper", this.isFromDropper());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("FizzleTicks")) {
            this.setFizzleTicks(nbt.getInt("FizzleTicks"));
        }
        if (nbt.contains("FromDropper")) {
            this.setFromDropper(nbt.getBoolean("FromDropper"));
        }
    }

    public int getFizzleTicks() {
        return this.entityData.get(FIZZLE_TICKS_ID);
    }

    public void setFizzleTicks(int fizzleTicks) {
        this.entityData.set(FIZZLE_TICKS_ID, fizzleTicks);
    }

    public boolean isFromDropper() {
        return this.entityData.get(FROM_DROPPER_ID);
    }

    public void setFromDropper(boolean fromDropper) {
        this.entityData.set(FROM_DROPPER_ID, fromDropper);
    }

    public int getWiggle() {
        return this.entityData.get(WIGGLE_ID);
    }

    public void setWiggle(int wiggle) {
        this.entityData.set(WIGGLE_ID, wiggle);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_ID_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public void setHurtTime(int hurtTime) {
        this.entityData.set(DATA_ID_HURT, hurtTime);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public void setHurtDir(int hurtDir) {
        this.entityData.set(DATA_ID_HURTDIR, hurtDir);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }
}
