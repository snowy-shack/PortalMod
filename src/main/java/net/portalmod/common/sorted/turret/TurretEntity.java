package net.portalmod.common.sorted.turret;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.entity.TestElementEntity;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.util.ModUtil;

import java.util.Collections;

public class TurretEntity extends TestElementEntity {
    public TurretEntity(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    public TurretEntity(World level) {
        super(EntityInit.TURRET.get(), level);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes();
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
    public void tick() {
        super.tick();

        this.yBodyRot = this.yRot;
    }

    // Copy of Cube.rideTick()
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
}