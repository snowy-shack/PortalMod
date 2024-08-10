package net.portalmod.common.sorted.cube;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.button.SuperButtonBlock;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import java.util.ArrayList;
import java.util.List;

public class Cube extends TestElementEntity {

    public double oldDeltaY = 0;
    public boolean oldActive = true;
    public boolean wasOnGround = true;
    public float wasVerticalSpeed = 0;

    public Cube(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }
    
    public static DamageSource cube(LivingEntity entity) {
        return new EntityDamageSource("cube", entity);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes();
    }

    @Override
    public void remove() {
        super.remove();
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
        return entity instanceof PlayerEntity && !entity.hasPassenger(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isFizzling()) {

            // todo only super button
            if (!this.level.isClientSide && this.isPassenger() && this.isActive() && !this.oldActive) {
                CriteriaTriggerInit.CUBE_ON_BUTTON.get().trigger((ServerPlayerEntity) this.getVehicle());
            }

            this.oldActive = isActive();

//        System.out.println(this.position());
//        System.out.println(this.getDeltaMovement());
//        System.out.println(((ITeleportable)this).getLastUsedPortal());

//        System.out.println((this.level.isClientSide ? "c " : "") + this.position().equals(new Vector3d(this.xo, this.yo, this.zo)));

            // todo better damage system

            AxisAlignedBB aabb = getBoundingBox().inflate(1.0E-7D);
            List<Entity> entities = this.level.getEntities(this, aabb.inflate(1.0E-1D));

            for (Entity entity : entities) {
                AxisAlignedBB entityAABB = entity.getBoundingBox();
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

            if (this.isOnGround() && !this.wasOnGround && oldDeltaY < -0.3) {
                System.out.println(oldDeltaY);
                float volume = MathHelper.clamp((float) -this.oldDeltaY, 0, 1);
                this.level.playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.CUBE_HIT.get(), SoundCategory.NEUTRAL, volume, ModUtil.randomSoundPitch());
            }

            this.wasOnGround = this.isOnGround();

            // push when entity near (copied from BoatEntity)
            this.checkInsideBlocks();
            List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate(0.05, -0.01, 0.05), EntityPredicates.pushableBy(this));
            for(Entity entity : list) {
                this.push(entity);
            }
        }

        this.yBodyRot = this.yRot;

        oldDeltaY = getDeltaMovement().y;

        this.yHeadRot = this.yBodyRot;
    }

    public void onSpawnedByPlayer() {
        this.level.playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.CUBE_HIT.get(), SoundCategory.NEUTRAL, 0.5f, 1);
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
                    BlockState state = this.level.getBlockState(pos);
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
    public ItemStack getItemBySlot(EquipmentSlotType slotType) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlotType slotType, ItemStack itemStack) {}

    @Override
    public HandSide getMainArm() {
        return HandSide.RIGHT;
    }
}