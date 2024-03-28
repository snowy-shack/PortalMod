package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import net.portalmod.common.sorted.portal.IClientTeleportable;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.interfaces.IGetPose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IClientTeleportable, IGetPose {
    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    @Shadow @Nullable private Pose forcedPose;

    @Shadow @Final public PlayerAbilities abilities;
    @Unique
    private boolean clientJustPortaled = false;

    @Override
    public void setJustPortaled(boolean justPortaled) {
        this.clientJustPortaled = justPortaled;
    }

    @Override
    public boolean getJustPortaled() {
        return this.clientJustPortaled;
    }

    @Override
    public void removeJustPortaled() {
        this.clientJustPortaled = false;
    }

    @Redirect(
            remap = false,
            method = "maybeBackOffFromEdge",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;noCollision(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Z"
            )
    )
    private boolean pmMaybeBackOffFromEdgeCube(World level, Entity entity, AxisAlignedBB bb) {
        return level.noCollision(entity, bb) && level.getEntities(entity, bb.inflate(1.0E-7D), cube ->
                cube instanceof Cube &&
                cube.getBoundingBox().intersects(bb) &&
                !entity.isPassengerOfSameVehicle(cube)
        ).stream().map(Entity::getBoundingBox).map(VoxelShapes::create).allMatch(VoxelShape::isEmpty);
    }

    @Unique
    private boolean pmWasFlying = false;

    @Inject(
            remap = false,
            method = "travel",
            at = @At("HEAD")
    )
    private void pmFixStopFlyingVelocity(CallbackInfo info) {
        PlayerEntity thiss = (PlayerEntity)(Object)this;

        if(thiss.abilities.flying && !thiss.isPassenger()) {
            this.pmWasFlying = true;
        } else {
            if(this.pmWasFlying) {
                double gravity = Objects.requireNonNull(thiss.getAttribute(ForgeMod.ENTITY_GRAVITY.get())).getValue();
                Vector3d d = thiss.getDeltaMovement();
                thiss.setDeltaMovement(d.x, -gravity, d.z);
            }

            this.pmWasFlying = false;
        }
    }

    public Pose pmGetNextPose() {
        if(this.forcedPose != null)
            return forcedPose;

        if(this.canEnterPose(Pose.SWIMMING)) {
            Pose pose;
            if(this.isFallFlying()) {
                pose = Pose.FALL_FLYING;
            } else if(this.isSleeping()) {
                pose = Pose.SLEEPING;
            } else if(this.isSwimming()) {
                pose = Pose.SWIMMING;
            } else if(this.isAutoSpinAttack()) {
                pose = Pose.SPIN_ATTACK;
            } else if(this.isShiftKeyDown() && !this.abilities.flying) {
                pose = Pose.CROUCHING;
            } else {
                pose = Pose.STANDING;
            }

            Pose pose1;
            if(!this.isSpectator() && !this.isPassenger() && !this.canEnterPose(pose)) {
                if(this.canEnterPose(Pose.CROUCHING)) {
                    pose1 = Pose.CROUCHING;
                } else {
                    pose1 = Pose.SWIMMING;
                }
            } else {
                pose1 = pose;
            }

            return pose1;
        }

        return this.getPose();
    }

    @Inject(
            remap = false,
            method = "startFallFlying",
            at = @At("HEAD")
    )
    private void pmGrantFaithPlateElytraAdvancement(CallbackInfo info) {
        // todo readd launched reset
        if(!this.level.isClientSide) {
            if(((IFaithPlateLaunchable)this).isLaunched() && ((PlayerEntity)(Object)this).getDeltaMovement().length() > .7) {
                CriteriaTriggerInit.FAITH_PLATE_ELYTRA.get().trigger((ServerPlayerEntity)(Object)this);
            }
        }
    }

    @Inject(
            remap = false,
            method = "getHurtSound",
            at = @At("HEAD"),
            cancellable = true
    )
    public void pmPlayGooHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> cir) {
        if (damageSource == FluidInit.GOO_DAMAGE) {
            cir.setReturnValue(SoundInit.GOO_DAMAGE.get());
        }
    }
}