package net.portalmod.common.entities;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.particles.FizzleFlakeParticle;
import net.portalmod.common.particles.FizzleGlowParticle;
import net.portalmod.common.particles.PortalGunSparkParticle;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunInteraction;
import net.portalmod.core.init.EntityTagInit;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;

import java.util.List;
import java.util.Random;

/**
 * Entity which can be fizzled, dropped by a cube dropper, picked up using a portal gun and broken with a wrench.
 */
public abstract class TestElementEntity extends LivingEntity implements Fizzleable {

    public static final DataParameter<Integer> FIZZLE_TICKS_ID = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> FROM_DROPPER_ID = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WIGGLE_ID = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> DATA_ID_HURT = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> DATA_ID_HURTDIR = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.INT);
    private static final DataParameter<Float> DATA_ID_DAMAGE = EntityDataManager.defineId(TestElementEntity.class, DataSerializers.FLOAT);

    public static final float HOLDING_DISTANCE = 1.5f;

    public int maxFizzleTime = 35;
    public boolean canFizzle;
    public Vec3 serverOldPos;

    private Vec3 oldRidingVec;
    private boolean holderJustTeleported;
    private Mat4 holderJustTeleportedMatrix;
    private int oldPortalChainLength;
    private Float eyeHeightOld;

    public TestElementEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
        this.canFizzle = true;
    }

    @Override
    public void tick() {
        if (this.getWiggle() > 0) {
            this.setWiggle(this.getWiggle() - 1);
        }

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        super.tick();

        if (this.isFizzling()) {
            this.fizzleTick();
        }
    }

    @Override
    public boolean shouldCheckForFizzlers() {
        return !this.isFizzling();
    }

    @Override
    public void onTouchingFizzler() {
        if (!this.isFizzling()) {
            this.startFizzling();
            this.fizzleTick();
        }
    }

    public boolean isFizzling() {
        return this.getFizzleTicks() > 0;
    }

    public void startFizzling() {
        if (canFizzle && !this.isFizzling()) {
            this.setFizzleTicks(this.getFizzleTicks() + 1);
        }
    }

    public int getFizzleLight(int packedLight) {
        int fizzleAmount = (int) (this.getFizzleTicks() * 0.6);
        return LightTexture.pack(
                Math.max(0, LightTexture.block(packedLight) - fizzleAmount),
                Math.max(0, LightTexture.sky(packedLight) - fizzleAmount)
        );
    }

    public void fizzleTick() {
        int fizzleTicks = this.getFizzleTicks();

        if (fizzleTicks == 1) {
            this.fizzleInit();
        }

        double minSpeed = 0.08;
        Vector3d xzMovement = this.getDeltaMovement().multiply(1, 0, 1);

        // If no horizontal movement, choose randomly
        if (xzMovement.length() == 0) {
            xzMovement = new Vector3d(new Random().nextFloat() * 0.1 - 0.05, 0, new Random().nextFloat() * 0.1 - 0.05);
        }

        Vector3d newMovement = xzMovement.length() < minSpeed ? xzMovement.normalize().scale(minSpeed) : xzMovement.scale(0.95);
        this.setDeltaMovement(newMovement);

        if (this.level.isClientSide) {
            FizzleGlowParticle.createGlowParticles(level, this);
            FizzleFlakeParticle.createFlakeParticles(level, this);
        }

        // Rotate slowly
        this.yRot += 35f * (float) newMovement.length();
        this.yHeadRot = this.yRot;

        if (fizzleTicks > this.maxFizzleTime && this.isAlive()) {
            this.fizzleKill();
        }

        this.setFizzleTicks(fizzleTicks + 1);
    }

    /**
     * On the first tick of fizzling
     */
    public void fizzleInit() {
        this.setNoGravity(true);
        this.level.playSound(null, this.position().x, this.position().y, this.position().z, SoundInit.ENTITY_FIZZLE.get(), SoundCategory.NEUTRAL, 1, 1);
    }

    /**
     * On the last tick of fizzling
     */
    public void fizzleKill() {
        if (!this.isFromDropper() && !this.getType().is(EntityTagInit.FIZZLER_NO_ITEM_DROPS) && !this.level.isClientSide) {
            this.dropAllDeathLoot(new DamageSource("fizzle"));
        }
        this.remove();
    }

    public void awardKill() {
        Entity holder = this.getVehicle();

        if (this.isPassenger() && holder instanceof PlayerEntity) {
            ((PlayerEntity) holder).awardStat(Stats.ENTITY_KILLED.get(this.getType()));

            if (holder instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(
                        (ServerPlayerEntity) holder,
                        this,
                        DamageSource.playerAttack((PlayerEntity) holder)
                );
            }
        }
    }

    public boolean pickUp(PlayerEntity player) {
        boolean riding = this.startRiding(player);
        if (!riding) return false;

        if (player.level.isClientSide()) {
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.PICK_ENTITY).data(this.getId()).build());
        }

        return true;
    }

    @Override
    public void rideTick() {
        this.setDeltaMovement(Vector3d.ZERO);
        this.tick();

        if (!(this.getVehicle() instanceof PlayerEntity)) {
            if (this.getVehicle() instanceof AbstractMinecartEntity || this.getVehicle() instanceof BoatEntity) {
                super.rideTick();
            } else {
                this.stopRiding();
            }
            return;
        }

        PlayerEntity player = (PlayerEntity)getVehicle();

        if(!this.level.isClientSide) {
            // server assigns old position stored in serverOldPos
            // since the former resets every tick
            // and thus cannot be assigned from the packet itself
            if(this.serverOldPos != null) {
                this.xo = this.serverOldPos.x;
                this.yo = this.serverOldPos.y;
                this.zo = this.serverOldPos.z;
                this.xOld = this.serverOldPos.x;
                this.yOld = this.serverOldPos.y;
                this.zOld = this.serverOldPos.z;
            }
        } else {
            Vec3 eyePos = new Vec3(player.getEyePosition(1).add(0, -0.4, 0));
            Vec3 eyeOldPos = new Vec3(player.getEyePosition(0).add(0, -0.4, 0));
            Vec3 originalEyePos = eyePos.clone();

            // account for eye height
            if(eyeHeightOld != null) {
                eyeOldPos = eyeOldPos.sub(0, player.getEyeHeight(), 0);
                eyeOldPos.add(0, eyeHeightOld, 0);
            }
            eyeHeightOld = player.getEyeHeight();
            Vec3 originalEyeOldPos = eyeOldPos.clone();

            Vec3 ridingVersor = new Vec3(0, 0, 1)
                    .transform(new Mat4(Vector3f.XP.rotationDegrees(player.getViewXRot(1))))
                    .transform(new Mat4(Vector3f.YN.rotationDegrees(player.getViewYRot(1))));

            Vec3 ridingVec = ridingVersor.clone().mul(HOLDING_DISTANCE);

            // get portals along the holding ray
            Vec3 from = eyePos;
            Vec3 to = eyePos.clone().add(ridingVec);
            List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(this.level, from, to, portal -> true);
            boolean passedPortal = portalChain.size() != this.oldPortalChainLength;
            this.oldPortalChainLength = portalChain.size();

            // compute the matrices from them
            Mat4 portalMatrix = Mat4.identity();
            Mat4 portalRotationMatrix = Mat4.identity();
            for(PortalEntity portal : portalChain) {
                if(!portal.getOtherPortal().isPresent())
                    break;

                Mat4 matrix = portal.getSourceBasis().getChangeOfBasisMatrix(portal.getOtherPortal().get().getDestinationBasis());

                portalMatrix = Mat4.identity()
                        .translate(new Vec3(portal.getOtherPortal().get().position()))
                        .mul(matrix)
                        .translate(new Vec3(portal.position()).negate())
                        .mul(portalMatrix);

                portalRotationMatrix = Mat4.identity()
                        .mul(matrix)
                        .mul(portalRotationMatrix);
            }

            eyePos = eyePos.transform(portalMatrix);
            eyeOldPos = eyeOldPos.transform(portalMatrix);
            ridingVec = ridingVec.transform(portalRotationMatrix);
            ridingVersor = ridingVersor.transform(portalRotationMatrix);

            // calculate rotation
            Vec3 lookVec = new Vec3(0, 0, 1)
                    .transform(new Mat4(Vector3f.YN.rotationDegrees(this.getVehicle().getYHeadRot())))
                    .transform(portalRotationMatrix);
            this.yRot = -(float)(Math.atan2(lookVec.x, lookVec.z) * 180 / Math.PI);
            this.yBodyRot = this.yRot;

            // old position
            Vec3 oldRidingVec = this.oldRidingVec;
            if(oldRidingVec == null) {
                Vec3 unteleportedOldRidingVec = new Vec3(this.position());
                for(int i = portalChain.size() - 1; i >= 0; i--) {
                    PortalEntity portal = portalChain.get(i);
                    if(!portal.getOtherPortal().isPresent())
                        break;

                    Mat4 matrix = portal.getOtherPortal().get().getSourceBasis().getChangeOfBasisMatrix(portal.getDestinationBasis());
                    unteleportedOldRidingVec = unteleportedOldRidingVec.sub(portal.getOtherPortal().get().position()).transform(matrix).add(portal.position());
                }

                oldRidingVec = unteleportedOldRidingVec.clone().sub(originalEyeOldPos);
            }

            oldRidingVec = oldRidingVec.transform(portalRotationMatrix);

            if(this.holderJustTeleported) {
                if(this.holderJustTeleportedMatrix != null) {
                    // holding client
                    oldRidingVec = oldRidingVec.transform(this.holderJustTeleportedMatrix);
                    this.holderJustTeleportedMatrix = null;
                } else {
                    // server
                    oldRidingVec = ridingVersor.clone().mul(oldRidingVec.magnitude());
                }

                this.holderJustTeleported = false;
            } else if(passedPortal) {
                this.yRotO = this.yRot;
                this.yBodyRotO = this.yBodyRot;
            }

            // teleport entity every tick
            Vec3 oldPos = eyeOldPos.clone().add(oldRidingVec);
            Vec3 center = new Vec3(this.getBoundingBox().getCenter()).sub(0, this.getBoundingBox().getYsize() / 2, 0);
            this.setBoundingBox(this.getBoundingBox().move(center.negate().to3d()).move(oldPos.to3d()));
            this.setPosAndOldPos(oldPos.x, oldPos.y, oldPos.z);

            // move the entity
            Vector3d ridingPos = eyePos.clone().add(ridingVec).to3d();
            this.move(MoverType.SELF, ridingPos.subtract(ModUtil.getOldPos(this)));

            // tell the server
            PacketInit.INSTANCE.sendToServer(new CTestElementHoldingPacket(this.getId(),
                    new Vec3(ModUtil.getOldPos(this)), new Vec3(this.position()), this.yBodyRotO, this.yBodyRot));

            // detach if too far
            if(this.position().distanceTo(ridingPos) > 1) {
                dropHeldEntities(player, false, true, player.getMainHandItem());
                PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.RELEASE_ENTITY).build());
            }

            // calculate unteleported position for next tick
            Vec3 unteleportedPos = new Vec3(this.position());

            for(int i = portalChain.size() - 1; i >= 0; i--) {
                PortalEntity portal = portalChain.get(i);
                if(!portal.getOtherPortal().isPresent())
                    break;

                Mat4 matrix = portal.getOtherPortal().get().getSourceBasis().getChangeOfBasisMatrix(portal.getDestinationBasis());
                unteleportedPos = unteleportedPos.sub(portal.getOtherPortal().get().position()).transform(matrix).add(portal.position());
            }

            this.oldRidingVec = unteleportedPos.clone().sub(originalEyePos);
        }

        this.fallDistance = 0;

        if (player.level.isClientSide) {
            if (player.getMainHandItem().getItem() instanceof PortalGun) {
                PortalGunSparkParticle.createParticles(this.level, player, false);
            } else if (player.getOffhandItem().getItem() instanceof PortalGun) {
                PortalGunSparkParticle.createParticles(this.level, player, true);
            }
        }

        if (this.isFizzling()) {
            this.awardKill();
            dropHeldEntities(player, false, false, player.getMainHandItem());
        }
    }

    public void onHolderTeleport(PortalEntity from, PortalEntity to) {
        if(this.holderJustTeleportedMatrix == null)
            this.holderJustTeleportedMatrix = Mat4.identity();

        Mat4 matrix = from.getSourceBasis().getChangeOfBasisMatrix(to.getDestinationBasis());
        this.holderJustTeleportedMatrix = matrix.mul(this.holderJustTeleportedMatrix);

        this.holderJustTeleported = true;
    }

    public void onHolderTeleportPacket() {
        this.holderJustTeleported = true;
    }

    @Override
    public boolean startRiding(Entity entity) {
        this.oldRidingVec = null;
        return super.startRiding(entity);
    }

    @Override
    public void stopRiding() {
        Entity vehicle = this.getVehicle();

        this.removeVehicle();
        if (!(vehicle instanceof PlayerEntity)) return;

        this.boardingCooldown = 0;

        Vector3d momentum;
        if(this.level.isClientSide) {
            momentum = this.position().subtract(ModUtil.getOldPos(this));
        } else {
            momentum = this.position().subtract(this.serverOldPos.to3d());
        }

        // Random movement so the cube cant fall perfectly onto a player which makes them swim
        this.setDeltaMovement(momentum.add(
                ModUtil.symmetricRandom(0.01f),
                0,
                ModUtil.symmetricRandom(0.01f)));
    }

    public static void dropHeldEntities(PlayerEntity player, boolean yeet, boolean nullifyMomentum, ItemStack itemStack) {
        List<Entity> passengers = player.getPassengers();
        for(int i = passengers.size() - 1; i >= 0; --i) {
            Entity entity = passengers.get(i);
            if(!(entity instanceof TestElementEntity)) {
                continue;
            }

            entity.stopRiding();

            if(itemStack.getItem() instanceof PortalGun) {
                PortalGun.dropCube(player, itemStack);
            }

            final float maxSpeed = 0.5f;
            final float strength = .3f;

            Vector3d velocity = entity.getDeltaMovement();
            boolean exceedsLimit = velocity.add(player.getDeltaMovement().reverse()).length() > maxSpeed;

            if(nullifyMomentum) {
                entity.setDeltaMovement(Vector3d.ZERO);
            } else {
                if(exceedsLimit) {
                    entity.setDeltaMovement(velocity.normalize().multiply(maxSpeed, maxSpeed, maxSpeed).add(player.getDeltaMovement()));
                }

                if(yeet) {
                    entity.setDeltaMovement(velocity.add(player.getViewVector(0).multiply(strength, strength, strength)));
                }
            }
        }
    }

    @Override
    public boolean isControlledByLocalInstance() {
        Entity entity = this.getVehicle();
        if(entity instanceof PlayerEntity) {
            return ((PlayerEntity)entity).isLocalPlayer();
        } else {
            return !this.level.isClientSide;
        }
    }

    public boolean isHoldable() {
        return !this.isFizzling();
    }

    public static boolean isHoldable(Entity entity) {
        return entity instanceof TestElementEntity && ((TestElementEntity) entity).isHoldable();
    }

    public boolean isPickedUp() {
        return this.getVehicle() != null && this.getVehicle() instanceof PlayerEntity;
    }

    public void dropIfPickedUp() {
        if (this.isPickedUp()) {
            PlayerEntity player = (PlayerEntity) this.getVehicle();
            assert player != null;
            dropHeldEntities(player, false, false, player.getMainHandItem());
        }
    }

    @Override
    public void remove(boolean keepData) {
        super.remove(keepData);
        this.dropIfPickedUp();
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.level.isClientSide || !this.isAlive()) {
            return false;
        }

        boolean shouldSwing = true;
        boolean holdingWrench = source instanceof EntityDamageSource && source.getEntity() instanceof LivingEntity && WrenchItem.hitWithWrench((LivingEntity) source.getEntity());
        boolean outOfWorld = source == DamageSource.OUT_OF_WORLD;
        boolean inGoo = source == FluidInit.GOO_DAMAGE && this.getVehicle() == null;
        boolean isCreative = source instanceof EntityDamageSource && source.getEntity() instanceof PlayerEntity && ((PlayerEntity) source.getEntity()).isCreative();

        boolean shouldHurt = holdingWrench || inGoo || outOfWorld || isCreative;

        if (holdingWrench) {
            damage *= 1.5f;
//            this.playSound(SoundInit.CUBE_HIT.get(), 0.75f, ModUtil.randomSoundPitch());
        }

        if (inGoo) {
            damage *= 0.1f;
            shouldSwing = false;
        }

        if (shouldHurt) {
            this.applyDamage(source, damage, shouldSwing, isCreative);
            return true;
        }

        return false;
    }

    public void applyDamage(DamageSource source, float damage, boolean swing, boolean creative) {
        if (swing) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
        }
        this.setDamage(this.getDamage() + damage * 10.0F);
        this.markHurt();
        if (creative || this.getDamage() > 40.0F) {
            this.remove();
            if (!creative && !this.isFromDropper() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                this.dropAllDeathLoot(source);
            }
        }
    }

    @Override
    public void animateHurt() {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FIZZLE_TICKS_ID, 0);
        this.entityData.define(FROM_DROPPER_ID, false);
        this.entityData.define(WIGGLE_ID, 0);
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, 0.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("FizzleTicks", this.getFizzleTicks());
        nbt.putBoolean("FromDropper", this.isFromDropper());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("FizzleTicks")) {
            this.setFizzleTicks(nbt.getInt("FizzleTicks"));
        }
        if (nbt.contains("FromDropper")) {
            this.setFromDropper(nbt.getBoolean("FromDropper"));
        }
    }

    public int getFizzleTicks() {
        return this.entityData.get(FIZZLE_TICKS_ID);
    }

    public void setFizzleTicks(int fizzleTicks) {
        this.entityData.set(FIZZLE_TICKS_ID, fizzleTicks);
    }

    public boolean isFromDropper() {
        return this.entityData.get(FROM_DROPPER_ID);
    }

    public void setFromDropper(boolean fromDropper) {
        this.entityData.set(FROM_DROPPER_ID, fromDropper);
    }

    public int getWiggle() {
        return this.entityData.get(WIGGLE_ID);
    }

    public void setWiggle(int wiggle) {
        this.entityData.set(WIGGLE_ID, wiggle);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_ID_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public void setHurtTime(int hurtTime) {
        this.entityData.set(DATA_ID_HURT, hurtTime);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public void setHurtDir(int hurtDir) {
        this.entityData.set(DATA_ID_HURTDIR, hurtDir);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }
}
