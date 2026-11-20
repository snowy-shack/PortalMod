package net.portalmod.common.sorted.cube;

import net.portalmod.core.init.DamageTypeInit;

import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.button.SuperButtonBlock;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class Cube extends TestElementEntity {

    public double oldDeltaY = 0;
    public boolean oldActive = true;
    public boolean wasOnGround = true;
    public float wasVerticalSpeed = 0;

    public Cube(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public static DamageSource cube(LivingEntity entity) {
        return DamageTypeInit.source(entity.level(), DamageTypeInit.CUBE, entity);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return entity instanceof Player
                && !entity.hasPassenger(this)
                && super.canCollideWith(entity);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getType() == EntityInit.GABE.get() && this.random.nextDouble() < 0.001) {
            this.playSound(SoundInit.CUBE_GABE.get(), 3, ModUtil.randomSlightSoundPitch());
        }

        if (!this.isFizzling()) {

            // todo only super button
            if (!this.level().isClientSide && this.isPassenger() && this.isActive() && !this.oldActive) {
                CriteriaTriggerInit.CUBE_ON_BUTTON.get().trigger((ServerPlayer) this.getVehicle());
            }

            this.oldActive = isActive();

//        System.out.println(this.position());
//        System.out.println(this.getDeltaMovement());
//        System.out.println(((ITeleportable)this).getLastUsedPortal());

//        System.out.println((this.level.isClientSide ? "c " : "") + this.position().equals(new Vector3d(this.xo, this.yo, this.zo)));

            // todo better damage system

            AABB aabb = getBoundingBox().inflate(1.0E-7D);
            List<Entity> entities = this.level().getEntities(this, aabb.inflate(1.0E-1D));

            for (Entity entity : entities) {
                AABB entityAABB = entity.getBoundingBox();
                double x0 = (aabb.maxX - entityAABB.minX);
                double x1 = -(aabb.minX - entityAABB.maxX);
                double y1 = -(aabb.minY - entityAABB.maxY);
                double z0 = (aabb.maxZ - entityAABB.minZ);
                double z1 = -(aabb.minZ - entityAABB.maxZ);

                double x = Math.min(x0, x1);
                double z = Math.min(z0, z1);

                if (y1 < x && y1 < z && getDeltaMovement().y > -.1 && oldDeltaY < -0.5f && entity instanceof LivingEntity) {
                    DamageSource damageSource = cube(this);
                    float damage = (float) oldDeltaY * -3;
                    ((LivingEntity) entity).getCombatTracker().recordDamage(damageSource, ((LivingEntity) entity).getHealth(), damage);
                    entity.hurt(damageSource, damage);
                }
            }

            if (this.onGround() && !this.wasOnGround && oldDeltaY < -0.3) {
                float volume = Mth.clamp((float) -this.oldDeltaY, 0, 1);
                this.level().playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.CUBE_HIT.get(), SoundSource.NEUTRAL, volume, ModUtil.randomSoundPitch());
            }

            this.wasOnGround = this.onGround();

            // push when entity near (copied from BoatEntity)
            this.checkInsideBlocks();
            List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.05, -0.01, 0.05), EntitySelector.pushableBy(this));
            for(Entity entity : list) {
                this.push(entity);
            }
        }

        this.yBodyRot = this.getYRot();

        oldDeltaY = getDeltaMovement().y;

        this.yHeadRot = this.yBodyRot;
    }

    public void onSpawnedByPlayer() {
        this.level().playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.CUBE_HIT.get(), SoundSource.NEUTRAL, 0.5f, ModUtil.randomSoundPitch());
    }

    @Override
    public boolean isPassenger() {
        return super.isPassenger();
    }

    public boolean isActive() {
        for(int z = -1; z <= 1; z++) {
            for(int y = -1; y <= 1; y++) {
                for(int x = -1; x <= 1; x++) {
                    BlockPos pos = new BlockPos(x, y, z).offset(this.blockPosition());
                    BlockState state = this.level().getBlockState(pos);
                    if(state.getBlock() == BlockInit.SUPER_BUTTON.get() && state.getValue(SuperButtonBlock.ACTIVE)) {
                        if(((SuperButtonBlock)BlockInit.SUPER_BUTTON.get()).getTrigger(state, pos).intersects(this.getBoundingBox())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected int calculateFallDamage(float f1, float f2) {
        return 0;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slotType) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slotType, ItemStack itemStack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}