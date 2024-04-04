package net.portalmod.common.entity;

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
import net.portalmod.common.sorted.cube.Cube;

public abstract class FizzleableEntity extends LivingEntity {

    public static final DataParameter<Integer> FIZZLE_TICKS_ID = EntityDataManager.defineId(Cube.class, DataSerializers.INT);

    public int fizzleTicks = 0;
    public int maxFizzleTime = 30;
    public boolean canFizzle;

    public FizzleableEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
        this.canFizzle = true;
    }

    //todo fix no visuals when fizzling a cube that you're not holding
    //todo track all intersected blocks between ticks

    @Override
    public void tick() {
        super.tick();
        if (isFizzling()) {
            double minSpeed = 0.08;
            Vector3d xzMovement = this.getDeltaMovement().multiply(1, 0, 1);
            Vector3d newMovement = xzMovement.length() < minSpeed ? xzMovement.normalize().scale(minSpeed) : xzMovement.scale(0.95);
            this.setDeltaMovement(newMovement);

            if (this.fizzleTicks > this.maxFizzleTime) {
                this.remove();
            }

            FizzleGlowParticle.createGlowParticles(level, this);
            FizzleFlakeParticle.createFlakeParticles(level, this);

            this.fizzleTicks++;
        }
    }

    public boolean isFizzling() {
        return this.fizzleTicks > 0;
    }

    public void startFizzling() {
        if (canFizzle && !this.isFizzling()) {
            this.fizzleTicks++;
            this.setNoGravity(true);
            this.level.playSound(null, this, SoundEvents.HORSE_DEATH, SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FIZZLE_TICKS_ID, this.fizzleTicks);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("FizzleTicks", this.fizzleTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        this.fizzleTicks = nbt.getInt("FizzleTicks");
    }
}
