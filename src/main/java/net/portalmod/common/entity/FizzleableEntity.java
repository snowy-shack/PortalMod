package net.portalmod.common.entity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.particles.FizzleFlakeParticle;
import net.portalmod.common.particles.FizzleGlowParticle;

import java.util.Random;

public abstract class FizzleableEntity extends LivingEntity {

    public static final DataParameter<Integer> FIZZLE_TICKS_ID = EntityDataManager.defineId(FizzleableEntity.class, DataSerializers.INT);

    public int maxFizzleTime = 30;
    public boolean canFizzle;

    public FizzleableEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
        this.canFizzle = true;
    }

    //todo track all intersected blocks between ticks

    @Override
    public void tick() {
        super.tick();

        int fizzleTicks = this.entityData.get(FIZZLE_TICKS_ID);
        if (fizzleTicks != this.getFizzleTicks()) {
            this.setFizzleTicks(fizzleTicks);
        }

        if (isFizzling()) {
            double minSpeed = 0.08;
            Vector3d xzMovement = this.getDeltaMovement().multiply(1, 0, 1);

            // If no horizontal movement, choose randomly
            if (xzMovement.length() == 0) {
                xzMovement = new Vector3d(new Random().nextFloat() * 0.1 - 0.05, 0, new Random().nextFloat() * 0.1 - 0.05);
            }

            Vector3d newMovement = xzMovement.length() < minSpeed ? xzMovement.normalize().scale(minSpeed) : xzMovement.scale(0.95);
            this.setDeltaMovement(newMovement);

            if (this.getFizzleTicks() > this.maxFizzleTime) {
                this.remove();
            }

            FizzleGlowParticle.createGlowParticles(level, this);
            FizzleFlakeParticle.createFlakeParticles(level, this);

            this.setFizzleTicks(this.getFizzleTicks() + 1);
        }
    }

    public boolean isFizzling() {
        return this.getFizzleTicks() > 0;
    }

    public void startFizzling() {
        if (canFizzle && !this.isFizzling()) {
            this.setFizzleTicks(this.getFizzleTicks() + 1);
            this.setNoGravity(true);
            this.level.playSound(null, this, SoundEvents.HORSE_DEATH, SoundCategory.BLOCKS, 1, 1);
        }
    }

    public int getFizzleLight(int packedLight) {
        int fizzleAmount = (int) (this.getFizzleTicks() * 0.75);
        return LightTexture.pack(
                Math.max(0, LightTexture.block(packedLight) - fizzleAmount),
                Math.max(0, LightTexture.sky(packedLight) - fizzleAmount)
        );
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FIZZLE_TICKS_ID, 0);
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
}
