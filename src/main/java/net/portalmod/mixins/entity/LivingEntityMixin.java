package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.ItemTagInit;
import net.portalmod.core.injectors.LivingEntityInjector;
import net.portalmod.core.interfaces.IDragCancelable;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Deque;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IFaithPlateLaunchable, IDragCancelable {
    private boolean pmLaunched = false;

    public LivingEntityMixin(EntityType<?> entityType, World level) {
        super(entityType, level);
    }

    @Shadow(remap = false) protected abstract SoundEvent getFallDamageSound(int i);

//    @Shadow protected int lerpSteps;

    @Shadow protected double lerpX;
    @Shadow protected double lerpY;
    @Shadow protected double lerpZ;

    @Shadow protected int lerpSteps;

    @Shadow protected abstract boolean isAffectedByFluids();

    @Shadow public abstract boolean canStandOnFluid(Fluid p_230285_1_);

    @Shadow public abstract Iterable<ItemStack> getArmorSlots();

    @Inject(
            remap = false,
            method = "aiStep",
            at = @At("HEAD")
    )
    private void pmLerpPosWithPortal(CallbackInfo info) {
        Deque<Tuple<Vector3d, Vector3d>> lerpPositions = ((ITeleportLerpable)this).getLerpPositions();
        if(lerpPositions.isEmpty() || !this.level.isClientSide)
            return;

        Tuple<Vector3d, Vector3d> currentLerpPos = lerpPositions.pop();
        this.setPos(currentLerpPos.getB().x, currentLerpPos.getB().y, currentLerpPos.getB().z);
        this.xo = currentLerpPos.getA().x;
        this.yo = currentLerpPos.getA().y;
        this.zo = currentLerpPos.getA().z;
        this.xOld = currentLerpPos.getA().x;
        this.yOld = currentLerpPos.getA().y;
        this.zOld = currentLerpPos.getA().z;
        this.lerpSteps = 0;
    }

    // has to redirect setPos after lerping
    @Redirect(
            remap = false,
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setPos(DDD)V",
                    ordinal = 0
            )
    )
    private void pmClientTeleport(LivingEntity instance, double x, double y, double z) {
        if(true) {
            instance.setPos(x, y, z);
            return;
        }

//        LivingEntity entity = (LivingEntity)(Object)this;
//
//        Deque<DiscontinuousLerpPos> lerpPosQueue = ((IDiscontinuouslyLerpable)entity).getLerpPosQueue();
//        if(lerpPosQueue.isEmpty())
//            return;
//
//        DiscontinuousLerpPos lerpPos = lerpPosQueue.peek();
//        if(lerpPos.isIncomplete())
//            return;
//
//        lerpPos.apply(entity);
//
//        if(lerpPos.isExtended()) {
//            if(lerpPos.isDone()) {
//                lerpPosQueue.poll();
//            } else {
//                lerpPos.consume();
//            }
//        } else {
//            lerpPosQueue.poll();
//        }

        PortalEntity portal = (PortalEntity)instance.level.getEntity(((ITeleportable)instance).getLastUsedPortal());

        if(!((ITeleportable)instance).hasLastUsedPortal() || portal == null) {
            instance.setPos(x, y, z);
            return;
        }

        PortalPair pair = PortalPairCache.CLIENT.get(portal.getGunUUID());
        if(pair == null) {
            instance.setPos(x, y, z);
            return;
        }

        PortalEntity targetPortal = pair.get(portal.getEnd().other());
        if(targetPortal == null) {
            instance.setPos(x, y, z);
            return;
        }

        Vector3f normal = new Vec3(portal.getDirection().getNormal()).to3f();
        Vector3f normal2 = new Vec3(portal.getDirection().getNormal()).to3f();
        normal.mul(.5f);
        normal2.mul(PortalRenderer.OFFSET * 10);
        Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
                .subtract(new Vector3d(normal)).add(new Vector3d(normal2));

        Vector3f targetnormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        Vector3f targetnormal2 = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        targetnormal.mul(.5f);
        targetnormal2.mul(PortalRenderer.OFFSET * 10);
        Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));

        Vector3d offset = targetPortalPos.subtract(portalPos);
        Vector3d portalToEntity = new Vector3d(x, y, z).subtract(portalPos);
        if(portalToEntity.dot(new Vec3(portal.getDirection().getNormal()).to3d()) >= 0) {
            instance.setPos(x, y, z);
            return;
        }

        AxisAlignedBB bb = instance.getBoundingBox();
        Vector3d delta = new Vector3d(x, y, z).subtract(instance.xo, instance.yo, instance.zo);
        AxisAlignedBB travelAABB = bb.expandTowards(delta);
        Vec3 projected = portal.projectPointOnPortalSurface(new Vec3(bb.getCenter().add(delta)));

        boolean isInside = projected.x > -.5
                && projected.x < .5
                && projected.y > -1
                && projected.y < 1;

        if(!portal.getBoundingBox().intersects(travelAABB) || !isInside) {
            instance.setPos(x, y, z);
            return;
        }

        instance.setPos(x + offset.x, y + offset.y, z + offset.z);
        instance.xo += offset.x;
        instance.yo += offset.y;
        instance.zo += offset.z;
        instance.xOld += offset.x;
        instance.yOld += offset.y;
        instance.zOld += offset.z;
        this.lerpX += offset.x;
        this.lerpY += offset.y;
        this.lerpZ += offset.z;
        ((ITeleportable)instance).removeLastUsedPortal();
    }

    @Unique
    private boolean pmCancelDrag = false;

    @Override
    public void pmSetCancelDrag(boolean cancelDrag) {
        this.pmCancelDrag = cancelDrag;
    }

    @Override
    public boolean pmIsCancelDrag() {
        return this.pmCancelDrag;
    }

    @Inject(
            remap = false,
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setDeltaMovement(DDD)V",
                    ordinal = 2,
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void pmCancelApplyDrag(Vector3d delta, CallbackInfo info, double d0, ModifiableAttributeInstance gravity, boolean flag, FluidState fluidstate, BlockPos blockpos, float f3, float f4, Vector3d vector3d5, double d2) {
        if(this.pmCancelDrag) {
//            double gravity = Objects.requireNonNull(entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get())).getValue();
//            entity.setDeltaMovement(x, entity.getDeltaMovement().y - gravity, z);
//            entity.setDeltaMovement(x, y / 0.98, z);
            ((LivingEntity)(Object)this).setDeltaMovement(vector3d5.x * (double)f4, d2, vector3d5.z * (double)f4);
//        } else {
////            entity.setDeltaMovement(x, y, z);
        }
    }

    @Redirect(
            remap = false,
            method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setDeltaMovement(DDD)V"),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/util/math/vector/Vector3d;F)Lnet/minecraft/util/math/vector/Vector3d;"
                    )
            )
    )
    private void pmDoLaunchedMovement(LivingEntity entity, double x, double y, double z) {
        Vector3d velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(x, velocity.y - 0.08, z);
//        entity.setDeltaMovement(velocity.x, velocity.y - 0.08, velocity.z);

//        if(true)
//            return;

//        Vector3d velocity = entity.getDeltaMovement();

        if(!pmLaunched) {
            entity.setDeltaMovement(x, y, z);
            return;
        }

        LivingEntity thiss = (LivingEntity)(Object)this;
        Vector3d deltaMovementFromLastTick = entity.position().subtract(new Vector3d(entity.xo, entity.yo, entity.zo));
        double momentumFromLastTick = Math.sqrt(LivingEntity.getHorizontalDistanceSqr(deltaMovementFromLastTick));

        if(entity.horizontalCollision || entity.verticalCollision)
            entity.setDeltaMovement(x, y, z);
        else
            entity.setDeltaMovement(velocity.x, velocity.y - 0.08, velocity.z);

        if(entity.horizontalCollision && !entity.verticalCollision && !entity.level.isClientSide
                && thiss.getItemBySlot(EquipmentSlotType.FEET).getItem() != ItemInit.LONGFALL_BOOTS.get()) {

            float f2 = (float)(momentumFromLastTick * 10 - 10);
            System.out.println(f2);
            if(f2 > 1) {
                // TODO new damage source
                entity.playSound(this.getFallDamageSound((int)f2), 1.0F, 1.0F);
                entity.hurt(DamageSource.FLY_INTO_WALL, f2);
            }
        }
    }
    
    @Inject(remap = false, at = @At("TAIL"), method = "tick()V")
    private void pmOnPostTick(CallbackInfo info) {
        LivingEntityInjector.onPostTick((LivingEntity)(Object)this);
    }

//    @Inject(remap = false, method = "lerpTo", at = @At("HEAD"), cancellable = true)
//    private void pmLerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean b, CallbackInfo info) {
//        World level = this.level;
//
//        AxisAlignedBB bb = this.getBoundingBox();
//        AxisAlignedBB travelAABB = bb.expandTowards(this.getDeltaMovement());
//
//        List<PortalEntity> portalsInEntity = level.getEntitiesOfClass(PortalEntity.class, travelAABB, portal -> {
//            if(!portal.isOpen())
//                return false;
//
//            PortalPair pair = PortalPairManager.CLIENT.get(portal.getGunUUID());
//            if(pair == null)
//                return false;
//
//            PortalEntity targetPortal = pair.get(portal.getEnd().other());
//            if(targetPortal == null)
//                return false;
//
//            Vec3 projected = portal.projectPointOnPortalSurface(new Vec3(bb.getCenter().add(this.getDeltaMovement())));
//
//            return projected.x > -.5
//                    && projected.x < .5
//                    && projected.y > -1
//                    && projected.y < 1;
//        });
//
//        if(portalsInEntity.isEmpty())
//            return;
//
//        PortalEntity portal = portalsInEntity.get(0);
//
//        PortalPair pair = PortalPairManager.CLIENT.get(portal.getGunUUID());
//        if(pair == null)
//            return;
//
//        PortalEntity targetPortal = pair.get(portal.getEnd().other());
//        if(targetPortal == null)
//            return;
//
//        Vector3f normal = portal.getDirection().step();
//        Vector3f normal2 = portal.getDirection().step();
//        normal.mul(.5f);
//        normal2.mul(PortalRenderer.OFFSET * 10);
//        Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
//                .subtract(new Vector3d(normal)).add(new Vector3d(normal2));
//
//        Vector3d entityPos = bb.getCenter().add(this.getDeltaMovement());
//        Vector3d distance = entityPos.subtract(portalPos);
//
//        if(distance.dot(new Vector3d(portal.getDirection().step())) >= 0)
//            return;
//
//        this.setPos(x, y, z);
//        this.setRot(xRot, yRot);
//        this.lerpSteps = 0;
//        info.cancel();
//    }
    
    @Override
    public void setLaunched(boolean launched) {
        pmLaunched = launched;
    }

    @Override
    public boolean isLaunched() {
        return pmLaunched;
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    public void pmAddGooResistance(Vector3d p_213352_1_, CallbackInfo ci) {
        FluidState fluidState = this.level.getFluidState(this.blockPosition());
        boolean isInGoo = !this.firstTick && this.fluidHeight.getDouble(FluidTagInit.GOO) > 0;

        if (isInGoo && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState.getType())) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.6, 0.3));
        }
    }

    @Inject(method = "baseTick", at = @At("HEAD"), remap = false)
    public void pmAddGooDamage(CallbackInfo ci) {
        boolean isInGoo = !this.firstTick && this.fluidHeight.getDouble(FluidTagInit.GOO) > 0;
        if (isInGoo) {
            this.fallDistance = 0;

            float damage = 4;
            for (ItemStack itemStack: this.getArmorSlots()) {
                if (itemStack.getItem().is(ItemTagInit.GOO_PROTECTION)) {
                    damage = 0.2f;
                    break;
                }
            }

            this.hurt(FluidInit.GOO_DAMAGE, damage);
        }
    }

    //todo might have to create a packet to hurt the entity on the client as well, will have to test this on a server
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;broadcastEntityEvent(Lnet/minecraft/entity/Entity;B)V"))
    public void pmHandleGooDamage(World instance, Entity entity, byte p_72960_2_, DamageSource p_70097_1_) {
        if (p_70097_1_ == FluidInit.GOO_DAMAGE && p_72960_2_ == 2 && entity instanceof LivingEntity) {
            this.level.playSound(null, entity, ((LivingEntity) entity).getHurtSound(p_70097_1_), SoundCategory.PLAYERS, 1, ModUtil.randomSoundPitch());
        } else {
            this.level.broadcastEntityEvent(this, p_72960_2_);
        }
    }
}