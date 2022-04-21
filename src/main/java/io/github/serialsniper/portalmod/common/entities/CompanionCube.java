package io.github.serialsniper.portalmod.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class CompanionCube extends LivingEntity {
    public static DamageSource cube(LivingEntity entity) {
        return new EntityDamageSource("companion_cube", entity);
    }

    private double oldDeltaY = 0;

    public CompanionCube(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes();
    }

    public boolean isPushable() {
        return true;
    }

    public boolean canCollideWith(Entity entity) {
        return true;
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
    }

    @Override
    public void tick() {
        super.tick();

        // todo better damage system

        AxisAlignedBB aabb = getBoundingBox().inflate(1.0E-7D);
        List<Entity> entities = this.level.getEntities(this, aabb.inflate(1.0E-1D));

//        System.out.println(getDeltaMovement().y);

        for(Entity entity : entities) {
            AxisAlignedBB entityAABB = entity.getBoundingBox();
            double x0 = (aabb.maxX - entityAABB.minX);
            double x1 = -(aabb.minX - entityAABB.maxX);
            double y1 = -(aabb.minY - entityAABB.maxY);
            double z0 = (aabb.maxZ - entityAABB.minZ);
            double z1 = -(aabb.minZ - entityAABB.maxZ);

            double x = Math.min(x0, x1);
            double z = Math.min(z0, z1);

            if(y1 < x && y1 < z && getDeltaMovement().y > -.1 && oldDeltaY < -0.5f)
                entity.hurt(cube(this), (float)oldDeltaY * -3);
        }

        oldDeltaY = getDeltaMovement().y;
    }

    @Override
    protected int calculateFallDamage(float p_225508_1_, float p_225508_2_) {
        return 0;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<ItemStack>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlotType p_184582_1_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlotType p_184201_1_, ItemStack p_184201_2_) {}

    @Override
    public HandSide getMainArm() {
        return HandSide.RIGHT;
    }
}