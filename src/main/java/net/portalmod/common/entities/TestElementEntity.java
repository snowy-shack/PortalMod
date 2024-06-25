package net.portalmod.common.entities;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.particles.FizzleFlakeParticle;
import net.portalmod.common.particles.FizzleGlowParticle;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.SoundInit;

import java.util.Random;

/**
 * Entity which can be fizzled and broken with a wrench.
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

    public TestElementEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
        this.canFizzle = true;
    }

    //todo track all intersected blocks between ticks

    @Override
    public void tick() {
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

        if (isFizzling()) {
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
                this.remove();
            }

            this.setFizzleTicks(this.getFizzleTicks() + 1);
        }
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
            if (!creative && !this.getFromDropper() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
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
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        this.setFizzleTicks(nbt.getInt("FizzleTicks"));
    }

    public int getFizzleTicks() {
        return this.entityData.get(FIZZLE_TICKS_ID);
    }

    public void setFizzleTicks(int fizzleTicks) {
        this.entityData.set(FIZZLE_TICKS_ID, fizzleTicks);
    }

    public boolean getFromDropper() {
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
