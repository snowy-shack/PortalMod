package net.portalmod.mixins.entity;

import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.portal.IClientTeleportable;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalHandler;
import net.portalmod.common.sorted.portal.PortalMovementAccounting;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.AttributeInit;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.init.DamageTypeInit;
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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IClientTeleportable, IGetPose, PortalHandler, Fizzleable {
    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow @Final public Abilities abilities;

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

    @Override
    public void onTeleport(PortalEntity from, PortalEntity to) {
        for(Entity passenger : this.getPassengers()) {
            if(passenger instanceof TestElementEntity) {
                ((TestElementEntity)passenger).onHolderTeleport(from, to);
            }
        }
    }

    @Override
    public void onTeleportPacket() {
        for(Entity passenger : this.getPassengers()) {
            if(passenger instanceof TestElementEntity) {
                ((TestElementEntity)passenger).onHolderTeleportPacket();
            }
        }
    }

    @Override
    public boolean shouldCheckForFizzlers() {
        return true;
    }

    @Override
    public void onTouchingFizzler() {
        Player player = (Player)(Object)this;

        if (player.isSpectator()) return;
        // Every connected client ticks remote-player replicas too, so without this guard each
        // client would send a FIZZLE packet for itself whenever any other player's replica
        // walked through a fizzler -- cascading into all online players' portals closing.
        // Only the client controlling this player (local) may send the packet; the server-side
        // tick (isClientSide=false) still runs as a best-effort fallback.
        if (player.level().isClientSide && !player.isLocalPlayer()) return;

        PortalGun.fizzleGunsInInventory(player);
    }

    @Inject(
                        method = "isSecondaryUseActive",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmUseWrenchSneaking(CallbackInfoReturnable<Boolean> info) {
        Player player = (Player)(Object)this;
        if(player.getMainHandItem().getItem() instanceof WrenchItem)
            info.setReturnValue(false);
    }

    @Redirect(
                        method = "maybeBackOffFromEdge",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"
            )
    )
    private boolean pmMaybeBackOffFromEdgeCube(Level level, Entity entity, AABB bb) {
        boolean noCollision = level.noCollision(entity, bb);

        boolean noCubeCollision = level.getEntities(entity, bb.inflate(1.0E-7D), cube ->
                cube instanceof Cube &&
                cube.getBoundingBox().intersects(bb) &&
                !entity.isPassengerOfSameVehicle(cube)
        ).stream().map(Entity::getBoundingBox).map(Shapes::create).allMatch(VoxelShape::isEmpty);

        boolean noPortalCollision = !Shapes.joinIsNotEmpty(PortalEntity.getCollisionShape(entity),
                Shapes.create(bb), BooleanOp.AND);

        return noCollision && noCubeCollision && noPortalCollision;
    }

    @Redirect(
                        method = "checkMovementStatistics",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"
            )
    )
    private void pmSuppressPortalTeleportMovementExhaustion(Player player, float exhaustion) {
        if(!PortalMovementAccounting.shouldSuppressMovementExhaustion(this.clientJustPortaled, exhaustion)) {
            player.causeFoodExhaustion(exhaustion);
        }
    }

    @Unique
    private boolean pmWasFlying = false;

    @Inject(
                        method = "travel",
            at = @At("HEAD")
    )
    private void pmFixStopFlyingVelocity(CallbackInfo info) {
        Player thiss = (Player)(Object)this;

        if(thiss.getAbilities().flying && !thiss.isPassenger()) {
            this.pmWasFlying = true;
        } else {
            if(this.pmWasFlying) {
                double gravity = 0.08D /* TODO(port): Forge ENTITY_GRAVITY attribute does not exist on Fabric; vanilla default */;
                Vec3 d = thiss.getDeltaMovement();
                thiss.setDeltaMovement(d.x, -gravity, d.z);
            }

            this.pmWasFlying = false;
        }
    }

    public Pose pmGetNextPose() {
        // (Forge's forcedPose does not exist in vanilla 1.20.1.)

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
                        method = "startFallFlying",
            at = @At("HEAD")
    )
    private void pmGrantFaithPlateElytraAdvancement(CallbackInfo info) {
        // todo readd launched reset
        if(!this.level().isClientSide) {
            if(((Flingable)this).isFlinging() && ((Player)(Object)this).getDeltaMovement().length() > .7) {
                CriteriaTriggerInit.FAITH_PLATE_ELYTRA.get().trigger((ServerPlayer)(Object)this);
            }
        }
    }


    @Inject(
                        method = "getHurtSound",
            at = @At("HEAD"),
            cancellable = true
    )
    public void pmPlayGooHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> cir) {
        if (damageSource.is(DamageTypeInit.GOO)) {
            cir.setReturnValue(SoundInit.GOO_DAMAGE.get());
        }
    }

    @Inject(
                        method = "createAttributes",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void pmAddAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .add(AttributeInit.GRAB_REACH.get())
                .add(AttributeInit.BUTTON_REACH.get())
        );
    }
}
