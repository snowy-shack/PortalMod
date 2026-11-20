package net.portalmod.mixins.entity;

import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.common.sorted.gel.IGelAffected;
import net.portalmod.common.sorted.goo.GooBlock;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.common.sorted.turret.TurretEntity;
import net.portalmod.core.init.DamageTypeInit;
import net.portalmod.core.init.ItemInit;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Deque;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Flingable, IDragCancelable, IGelAffected {
    private boolean pmLaunched = false;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract SoundEvent getFallDamageSound(int i);

//    @Shadow protected int lerpSteps;

    @Shadow protected double lerpX;
    @Shadow protected double lerpY;
    @Shadow protected double lerpZ;

    @Shadow protected int lerpSteps;

    @Shadow protected abstract boolean isAffectedByFluids();

    @Shadow public abstract boolean canStandOnFluid(Fluid p_230285_1_);

    @Shadow public abstract Iterable<ItemStack> getArmorSlots();

    @Shadow public abstract EntityDimensions getDimensions(Pose p_213305_1_);

    @Shadow public abstract Packet<ClientGamePacketListener> getAddEntityPacket();

    @Shadow public abstract void stopRiding();

    @Shadow protected abstract void checkFallDamage(double p_184231_1_, boolean p_184231_3_, BlockState p_184231_4_, BlockPos p_184231_5_);

    @Inject(
                        method = "aiStep",
            at = @At("HEAD")
    )
    private void pmLerpPosWithPortal(CallbackInfo info) {
        Deque<Tuple<net.minecraft.world.phys.Vec3, net.minecraft.world.phys.Vec3>> lerpPositions = ((ITeleportLerpable)this).getLerpPositions();
        if(lerpPositions.isEmpty() || !this.level().isClientSide)
            return;

        Tuple<net.minecraft.world.phys.Vec3, net.minecraft.world.phys.Vec3> currentLerpPos = lerpPositions.pop();
        this.setPos(currentLerpPos.getB().x, currentLerpPos.getB().y, currentLerpPos.getB().z);
        this.xo = currentLerpPos.getA().x;
        this.yo = currentLerpPos.getA().y;
        this.zo = currentLerpPos.getA().z;
        this.xOld = currentLerpPos.getA().x;
        this.yOld = currentLerpPos.getA().y;
        this.zOld = currentLerpPos.getA().z;
        this.lerpSteps = 0;
    }

    @Redirect(
                        method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInWall()Z"
            )
    )
    private boolean pmAvoidSuffocationInPortal(LivingEntity entity) {
        return !this.pmHasNearbyPortals(entity) && entity.isInWall();
    }

    @Redirect(
                        method = "checkFallDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void pmCheckPortalFallDamage(Entity entity, double heightDelta, boolean onGround, BlockState state, BlockPos pos) {
        if(this.pmHasNearbyPortals(entity)) {
            heightDelta = entity.position().y - entity.yo;
        }

        super.checkFallDamage(heightDelta, onGround, state, pos);
    }

    private boolean pmHasNearbyPortals(Entity entity) {
        net.minecraft.world.phys.Vec3 delta = new net.minecraft.world.phys.Vec3(entity.xo, entity.yo, entity.zo).subtract(entity.position());
        AABB aabb = entity.getBoundingBox().expandTowards(delta).inflate(2);
        return !PortalEntity.getPortals(entity.level(), aabb, portal -> true).isEmpty();
    }

    // has to redirect setPos after lerping
    @Redirect(
                        method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setPos(DDD)V",
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

        PortalEntity portal = (PortalEntity)instance.level().getEntity(((ITeleportable)instance).getLastUsedPortal());

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
        normal2.mul(PortalEntityRenderer.OFFSET * 10);
        net.minecraft.world.phys.Vec3 portalPos = net.minecraft.world.phys.Vec3.atCenterOf(portal.blockPosition())
                .subtract(new net.minecraft.world.phys.Vec3(normal)).add(new net.minecraft.world.phys.Vec3(normal2));

        Vector3f targetnormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        Vector3f targetnormal2 = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        targetnormal.mul(.5f);
        targetnormal2.mul(PortalEntityRenderer.OFFSET * 10);
        net.minecraft.world.phys.Vec3 targetPortalPos = net.minecraft.world.phys.Vec3.atCenterOf(targetPortal.blockPosition())
                .subtract(new net.minecraft.world.phys.Vec3(targetnormal)).add(new net.minecraft.world.phys.Vec3(targetnormal2));

        net.minecraft.world.phys.Vec3 offset = targetPortalPos.subtract(portalPos);
        net.minecraft.world.phys.Vec3 portalToEntity = new net.minecraft.world.phys.Vec3(x, y, z).subtract(portalPos);
        if(portalToEntity.dot(new Vec3(portal.getDirection().getNormal()).to3d()) >= 0) {
            instance.setPos(x, y, z);
            return;
        }

        AABB bb = instance.getBoundingBox();
        net.minecraft.world.phys.Vec3 delta = new net.minecraft.world.phys.Vec3(x, y, z).subtract(instance.xo, instance.yo, instance.zo);
        AABB travelAABB = bb.expandTowards(delta);
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

    @Inject(method = "tick", at = @At("RETURN"))
    public void checkFizzlers(CallbackInfo ci) {
        if (this instanceof Fizzleable) {
            ((Fizzleable) this).checkForFizzlers(this);
        }
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
                        method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V",
                    ordinal = 2,
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void pmCancelApplyDrag(net.minecraft.world.phys.Vec3 delta, CallbackInfo info, double d0, AttributeInstance gravity, boolean flag, FluidState fluidstate, BlockPos blockpos, float f3, float f4, net.minecraft.world.phys.Vec3 vector3d5, double d2) {
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
                        method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
                    )
            )
    )
    private void pmDoLaunchedMovement(LivingEntity entity, double x, double y, double z) {
        net.minecraft.world.phys.Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(x, velocity.y - 0.08, z);
//        entity.setDeltaMovement(velocity.x, velocity.y - 0.08, velocity.z);

//        if(true)
//            return;

//        net.minecraft.world.phys.Vec3 velocity = entity.getDeltaMovement();

        if(!pmLaunched) {
            entity.setDeltaMovement(x, y, z);
            return;
        }

        LivingEntity thiss = (LivingEntity)(Object)this;
        net.minecraft.world.phys.Vec3 deltaMovementFromLastTick = entity.position().subtract(new net.minecraft.world.phys.Vec3(entity.xo, entity.yo, entity.zo));
        double momentumFromLastTick = Math.sqrt(deltaMovementFromLastTick.horizontalDistanceSqr());

        if(entity.horizontalCollision || entity.verticalCollision)
            entity.setDeltaMovement(x, y, z);
        else
            entity.setDeltaMovement(velocity.x, velocity.y - 0.08, velocity.z);

        if(entity.horizontalCollision && !entity.verticalCollision && !entity.level().isClientSide
                && thiss.getItemBySlot(EquipmentSlot.FEET).getItem() != ItemInit.LONGFALL_BOOTS.get()) {

            float slamDamage = (float)(momentumFromLastTick * 3 - 10);
            if(slamDamage > .1F) {
                entity.playSound(this.getFallDamageSound((int)slamDamage), 1.0F, ModUtil.randomSlightSoundPitch());
                entity.hurt(DamageTypeInit.source(entity.level(), DamageTypeInit.FAITHPLATE_WALL), slamDamage);
            }
        }
    }
    
    @Inject(at = @At("TAIL"), method = "tick()V")
    private void pmOnPostTick(CallbackInfo info) {
        LivingEntityInjector.onPostTick((LivingEntity)(Object)this);
    }

//    @Inject(method = "lerpTo", at = @At("HEAD"), cancellable = true)
//    private void pmLerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean b, CallbackInfo info) {
//        Level level = this.level;
//
//        AABB bb = this.getBoundingBox();
//        AABB travelAABB = bb.expandTowards(this.getDeltaMovement());
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
//        net.minecraft.world.phys.Vec3 portalPos = net.minecraft.world.phys.Vec3.atCenterOf(portal.blockPosition())
//                .subtract(new net.minecraft.world.phys.Vec3(normal)).add(new net.minecraft.world.phys.Vec3(normal2));
//
//        net.minecraft.world.phys.Vec3 entityPos = bb.getCenter().add(this.getDeltaMovement());
//        net.minecraft.world.phys.Vec3 distance = entityPos.subtract(portalPos);
//
//        if(distance.dot(new net.minecraft.world.phys.Vec3(portal.getDirection().step())) >= 0)
//            return;
//
//        this.setPos(x, y, z);
//        this.setRot(xRot, yRot);
//        this.lerpSteps = 0;
//        info.cancel();
//    }
    
    @Override
    public void setFlinging(boolean launched) {
        pmLaunched = launched;
    }

    @Override
    public boolean isFlinging() {
        return pmLaunched;
    }

    private float lastNeutralHeight = 0;
    private boolean bounced = false;
    private boolean horizontalBounced = false;
    private boolean wasOnGround = true;
    private boolean leftGround = false;
    private net.minecraft.world.phys.Vec3 lastDeltaMovement = net.minecraft.world.phys.Vec3.ZERO;
    private net.minecraft.world.phys.Vec3 lastLastDeltaMovement = net.minecraft.world.phys.Vec3.ZERO;
    private int propulsionTicks = 0;

    @Override
    public int getPropulsionTicks() {
        return this.propulsionTicks;
    }

    @Override
    public void setPropulsionTicks(int ticks) {
        this.propulsionTicks = ticks;
    }

    @Override
    public void incrementPropulsionTicks() {
        this.propulsionTicks = Math.min(MAX_PROPULSION_TICKS, this.propulsionTicks + 1);
    }

    @Override
    public void decrementPropulsionTicks() {
        // Decrement at double speed
        this.propulsionTicks = Math.max(0, this.propulsionTicks - 2);
    }

    @Override
    public void setLastNeurtalHeight(float distance) {
        lastNeutralHeight = distance;
    }

    @Override
    public float getLastNeutralHeight() {
        return lastNeutralHeight;
    }

    @Override
    public void setBounced(boolean newBounced) {
        bounced = newBounced;
    }

    @Override
    public boolean getBounced() {
        return bounced;
    }

    @Override
    public void setHorizontalBounced(boolean newHorizontalBounced) {
        horizontalBounced = newHorizontalBounced;
    }

    @Override
    public boolean getHorizontalBounced() {
        return horizontalBounced;
    }

    @Override
    public void setWasOnGround(boolean newWasOnGround) {
        wasOnGround = newWasOnGround;
    }

    @Override
    public boolean getWasOnGround() {
        return wasOnGround;
    }

    @Override
    public void setLastDeltaMovement(net.minecraft.world.phys.Vec3 newLastDeltaMovement) {
        lastDeltaMovement = newLastDeltaMovement;
    }

    @Override
    public net.minecraft.world.phys.Vec3 getLastDeltaMovement() {
        return lastDeltaMovement;
    }

    @Override
    public void setLastLastDeltaMovement(net.minecraft.world.phys.Vec3 newLastLastDeltaMovement) {
        lastLastDeltaMovement = newLastLastDeltaMovement;
    }

    @Override
    public net.minecraft.world.phys.Vec3 getLastLastDeltaMovement() {
        return lastLastDeltaMovement;
    }

    @Override
    public boolean getLeftGround() {
        return leftGround;
    }

    @Override
    public void setLeftGround(boolean leftGround) {
        this.leftGround = leftGround;
    }

    @Override
    protected void spawnSprintParticle() {
        Level level = this.level();
        BlockPos pos = BlockPos.containing(this.position());
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof AbstractGelBlock)) {
            super.spawnSprintParticle();
        } else {
            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY() + (double)0.8F);
            int k = Mth.floor(this.getZ());
            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = this.level().getBlockState(blockpos);
            if(true) { // Forge addRunningEffects hook: vanilla always spawns the particles
                if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                    net.minecraft.world.phys.Vec3 vector3d = this.getDeltaMovement();
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate),
                            this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(this.getPose()).width,
                            this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(this.getPose()).width,
                            vector3d.x * -4.0D, 1.5D, vector3d.z * -4.0D);
                }
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        Level level = this.level();
        BlockPos nPos = BlockPos.containing(this.position());
        BlockState nState = level.getBlockState(nPos);

        if (!(nState.getBlock() instanceof AbstractGelBlock)) {
            super.playStepSound(pos, state);
        } else {
            SoundType soundtype = nState.getSoundType();
            this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, ModUtil.randomSoundPitch());
        }
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    public void pmAddGooResistance(net.minecraft.world.phys.Vec3 p_213352_1_, CallbackInfo ci) {
        FluidState fluidState = this.level().getFluidState(this.blockPosition());

        if (GooBlock.isInGoo(this) && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState.getType())) {
            GooBlock.applyGooResistance(this);
        }
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void pmAddGooDamage(CallbackInfo ci) {
        if (GooBlock.isInGoo(this)) {
            GooBlock.addGooDamage(this);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V", ordinal = 2))
    public void pmHandleGooDamage(Level world, Entity entity, byte b, DamageSource damageSource) {
        if (damageSource.is(DamageTypeInit.GOO) && b == 2 && entity instanceof LivingEntity) {
            GooBlock.handleGooDamage((LivingEntity) entity, damageSource);
        } else {
            this.level().broadcastEntityEvent(this, b);
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidJumpThreshold()D"))
    public void pmJumpInGoo(CallbackInfo ci) {
        if (GooBlock.isInGoo(this) && WrenchItem.holdingWrench(this)) {
            GooBlock.applyVerticalSwimSpeed((LivingEntity)(Object)this);
        }
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;stopRiding()V"))
    public void avoidTestElementEntityDismount(LivingEntity instance){
        if (!(instance instanceof TestElementEntity)) stopRiding();
    }

    @Inject(method = "canSee", at = @At("HEAD"), cancellable = true)
    public void pmCanSee(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof TurretEntity && !((TurretEntity) entity).getState().isStanding()) {
            cir.setReturnValue(false);
        }
    }
}