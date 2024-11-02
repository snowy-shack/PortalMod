package net.portalmod.common.entities;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
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
import net.portalmod.common.sorted.fizzler.FizzlerEmitterBlock;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.EntityTagInit;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

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
            this.handleFizzling();
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
            if (state.getBlock() instanceof FizzlerFieldBlock && FizzlerFieldBlock.getFieldShape(state).bounds().move(pos).intersects(movementBox)
                    || state.getBlock() instanceof FizzlerEmitterBlock && FizzlerEmitterBlock.getFieldShape(state).bounds().move(pos).intersects(movementBox)) {
                this.startFizzling();
                this.handleFizzling();
            }
        });
    }

    public void handleFizzling() {
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

        if (this.getFizzleTicks() > this.maxFizzleTime && !this.level.isClientSide && this.isAlive()) {
            if (!this.isFromDropper() && !this.getType().is(EntityTagInit.FIZZLER_NO_ITEM_DROPS)) {
                this.dropAllDeathLoot(new DamageSource("fizzle"));
            }
            this.remove();
        }

        this.setFizzleTicks(this.getFizzleTicks() + 1);
    }

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

        PortalGunSparkParticle.createParticles(this.level, player);

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
    public boolean hurt(DamageSource source, float damage) {
        if (this.level.isClientSide || !this.isAlive()) {
            return false;
        }

        boolean shouldSwing = true;
        boolean holdingWrench = source instanceof EntityDamageSource && source.getEntity() instanceof PlayerEntity && ((PlayerEntity) source.getEntity()).getMainHandItem().getItem() instanceof WrenchItem;
        boolean outOfWorld = source == DamageSource.OUT_OF_WORLD;
        boolean inGoo = source == FluidInit.GOO_DAMAGE && this.getVehicle() == null;
        boolean isCreative = source instanceof EntityDamageSource && source.getEntity() instanceof PlayerEntity && ((PlayerEntity) source.getEntity()).isCreative();

        boolean shouldHurt = holdingWrench || inGoo || outOfWorld || isCreative;

        if (holdingWrench) {
            damage *= 1.5f;
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
        this.markHurt();
        this.setDamage(this.getDamage() + damage * 10.0F);
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

    public boolean isFizzling() {
        return this.getFizzleTicks() > 0;
    }

    public void startFizzling() {
        if (canFizzle && !this.isFizzling()) {
            this.setFizzleTicks(this.getFizzleTicks() + 1);
            this.setNoGravity(true);
            this.level.playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.ENTITY_FIZZLE.get(), SoundCategory.NEUTRAL, 1, 1);

            if (this.isPassenger() && this.getVehicle() instanceof PlayerEntity) {
                ((PlayerEntity) this.getVehicle()).awardStat(Stats.ENTITY_KILLED.get(this.getType()));

                if (this.getVehicle() instanceof ServerPlayerEntity) {
                    CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(
                            (ServerPlayerEntity) this.getVehicle(),
                            this,
                            DamageSource.playerAttack((PlayerEntity) this.getVehicle())
                    );
                }
            }
        }
    }

    public int getFizzleLight(int packedLight) {
        int fizzleAmount = (int) (this.getFizzleTicks() * 0.6);
        return LightTexture.pack(
                Math.max(0, LightTexture.block(packedLight) - fizzleAmount),
                Math.max(0, LightTexture.sky(packedLight) - fizzleAmount)
        );
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
        this.setFizzleTicks(nbt.getInt("FizzleTicks"));
        this.setFromDropper(nbt.getBoolean("FromDropper"));
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
