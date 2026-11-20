package net.portalmod.common.sorted.portal;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.portalmod.core.network.PacketDistributor;
import net.portalmod.PortalMod;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.interfaces.IDragCancelable;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.packet.CPlayerPortalTeleportPacket;
import net.portalmod.core.util.DebugRenderer;
import net.portalmod.core.util.ModUtil;
import net.portalmod.mixins.accessors.EntityAccessor;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class PortalEntity extends Entity implements IEntityAdditionalSpawnData {
    private final float WIDTH = 1;
    private final float HEIGHT = 2;
    private final float DEPTH = 1/16f;

    private PortalEnd end = PortalEnd.NONE;
    private Direction direction = Direction.SOUTH;
    private Direction up = Direction.UP;
    private UUID gunUUID;
    private String hue = "blue";
    private int age = 0;

    public PortalEntity(EntityType<? extends PortalEntity> type, Level level) {
        super(type, level);
    }

    public PortalEntity(Level level) {
        super(EntityInit.PORTAL.get(), level);
    }

    @Override
    public void tick() {
        if(this.getY() < -64.0D)
            this.outOfWorld();
        if(!level.isClientSide && this.isAlive() && (!this.survives() || this.hasMoved()))
            this.remove();

        if(level.isClientSide) {
            PortalPhotonParticle.createLivingParticles(this);
        }

        this.age++;
    }

    @Override
    public boolean save(CompoundTag nbt) {
        return false;
    }

    public boolean saveGlobal(CompoundTag nbt) {
        this.removed = false;
        return super.save(nbt);
    }

    private boolean hasMoved() {
        return this.position().distanceToSqr(new net.minecraft.world.phys.Vec3(this.xo, this.yo, this.zo)) > 0;
    }

    public int getAge() {
        return this.age;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.recalculateBoundingBox();
        this.removed = false;
        this.age = 0;
    }

    public Vec3 teleportPoint(Vec3 point) {
        if(!this.isOpen())
            return point;

        Optional<PortalEntity> targetPortalOptional = this.getOtherPortal();
        if(!targetPortalOptional.isPresent()) {
            PartialPortal targetPartialPortal = ClientPortalManager.getInstance().getPartial(this.gunUUID, this.end.other());
            if(targetPartialPortal == null)
                return point;

            Mat4 portalToPortalMatrix = PortalEntity.getPortalToPortalMatrix(this, targetPartialPortal);
            return point.clone().transform(portalToPortalMatrix);
        }

        PortalEntity targetPortal = targetPortalOptional.get();
        Mat4 portalToPortalMatrix = PortalEntity.getPortalToPortalMatrix(this, targetPortal);
        return point.clone().transform(portalToPortalMatrix);
    }

    public Vec3 teleportVector(Vec3 vector) {
        if(!this.isOpen())
            return vector;

        Optional<PortalEntity> targetPortalOptional = this.getOtherPortal();
        if(!targetPortalOptional.isPresent()) {
            PartialPortal targetPartialPortal = ClientPortalManager.getInstance().getPartial(this.gunUUID, this.end.other());
            if(targetPartialPortal == null)
                return vector;

            Mat4 portalToPortalMatrix = PortalEntity.getPortalToPortalRotationMatrix(this, targetPartialPortal);
            return vector.clone().transform(portalToPortalMatrix);
        }

        PortalEntity targetPortal = targetPortalOptional.get();
        Mat4 portalToPortalMatrix = PortalEntity.getPortalToPortalRotationMatrix(this, targetPortal);
        return vector.clone().transform(portalToPortalMatrix);
    }

    public boolean canPointEnter(Vec3 point, boolean lenient) {
        Optional<PortalEntity> targetPortalOptional = this.getOtherPortal();
        if(!this.isOpen() || (!targetPortalOptional.isPresent() && !ClientPortalManager.getInstance().hasPartial(this.gunUUID, this.end.other())))
            return false;

        Vec3 portalPos = new Vec3(this.position());
        if(lenient) {
            portalPos.add(new Vec3(this.getNormal()).negate().mul(0.3));
        }

        Vec3 distance = point.clone().sub(portalPos);
        return distance.dot(this.getNormal()) < 0;
    }

    private static net.minecraft.world.phys.Vec3 recursivelyTeleportEntity(Entity entity, net.minecraft.world.phys.Vec3 delta, PortalEntity justExited, int depth) {
        if(depth > 100 || (!entity.level.isClientSide && entity instanceof Player))
            return delta;

        // Guard against broken/extreme movement state. Without this the pmCollide call below
        // expands the entity's AABB by delta and iterates every voxel shape inside it, which
        // can hang the server for many seconds (see watchdog crashes with large entity counts).
        if(!isFiniteVec(delta) || !isFiniteVec(entity.position()) || !isFiniteAABB(entity.getBoundingBox()))
            return delta;

        final double MAX_TELEPORT_DELTA = 64.0;
        if(delta.lengthSqr() > MAX_TELEPORT_DELTA * MAX_TELEPORT_DELTA)
            return delta;

        boolean inFluid = entity.isInWater() || entity.isInLava();
        boolean flying = entity instanceof Player && ((Player)entity).abilities.flying;
        boolean levitating = entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.LEVITATION);
        boolean shouldResetVelocityAtApex = !(entity instanceof FireworkRocketEntity || levitating);

        if(!inFluid && !entity.isOnGround() && !flying && shouldResetVelocityAtApex) {
            if(delta.y > 0 && delta.y * 0.98 - 0.08 < 0) {
                delta = new net.minecraft.world.phys.Vec3(delta.x, 0, delta.z);
                net.minecraft.world.phys.Vec3 dm = entity.getDeltaMovement();
                entity.setDeltaMovement(dm.x, 0, dm.z);
            }
        }

        Vec3 positionToCenterVector = new Vec3(entity.getBoundingBox().getCenter()).sub(entity.position());

        Level level = entity.level;

        // Do all cheap filters FIRST using an over-approximation of the travel path. pmCollide is
        // very expensive (it sweeps voxel shapes within the entity's expanded AABB) and previously
        // ran for every non-player entity tick, even ones that had no chance of teleporting. That
        // made the server hang with a relatively small number of minecarts.
        //
        // The conservative AABB (expanded by the raw delta rather than the post-collision delta)
        // is a superset of the real travel AABB, so any portal that would pass the real filter is
        // still in this candidate list.
        AABB conservativeTravelAABB = entity.getBoundingBox().expandTowards(delta);
        Vec3 entityOldCenter = new Vec3(entity.getBoundingBox().getCenter());

        List<PortalEntity> candidatePortals = getOpenPortals(level, conservativeTravelAABB, portal -> {
            boolean correctDirection = entity.getDeltaMovement().dot(new Vec3(portal.getNormal()).to3d()) < 0;
            if(!correctDirection)
                return false;

            // Already on / past the portal surface - can't "enter" it again this tick.
            if(portal.canPointEnter(entityOldCenter, true))
                return false;

            if(!portal.isEntityAlignedToPortal(entity))
                return false;

            return justExited == null
                    || new Vec3(portal.position()).sub(justExited.position()).dot(justExited.getNormal()) > 0
                    || portal == justExited;
        });

        if(candidatePortals.isEmpty())
            return delta;

        // Only now pay for the full voxel-shape collision sweep, and only for the tight predicate
        // that requires the true post-collision endpoint.
        net.minecraft.world.phys.Vec3 tmpDelta = ((EntityAccessor)entity).pmCollide(delta);
        AABB travelAABB = entity.getBoundingBox().expandTowards(tmpDelta);

        List<PortalEntity> portals = new ArrayList<>();
        for(PortalEntity portal : candidatePortals) {
            if(!portal.getBoundingBox().intersects(travelAABB))
                continue;

            Vec3 entityPos = entityOldCenter.clone().add(tmpDelta);
            if(portal.canPointEnter(entityPos, false))
                portals.add(portal);
        }

        if(portals.isEmpty())
            return delta;

        PortalEntity portal = portals.get(0);

        Vec3 teleportedCenter = portal.teleportPoint(new Vec3(entity.getBoundingBox().getCenter()));
        Vec3 oldTeleportedCenter = teleportedCenter.clone();

        if(portal.getOtherPortal().isPresent() && portal.getDirection().getAxis().isVertical() && portal.getOtherPortal().get().getDirection().getAxis().isHorizontal()) {
            teleportedCenter.y = portal.getOtherPortal().get().position().y;
        }

        Vec3 teleportedPos = teleportedCenter.clone().sub(positionToCenterVector);

        entity.setPosAndOldPos(teleportedPos.x, teleportedPos.y, teleportedPos.z);
        entity.setBoundingBox(entity.getBoundingBox()
                .move(new Vec3(entity.getBoundingBox().getCenter()).negate().to3d())
                .move(teleportedCenter.to3d()));

        Optional<PortalEntity> targetPortalOptional = portal.getOtherPortal();
        if(!portal.isOpen() || !targetPortalOptional.isPresent())
            return delta;

        PortalEntity targetPortal = targetPortalOptional.get();

        double gravity = 0.08;
        if(entity instanceof LivingEntity)
            gravity = Objects.requireNonNull(((LivingEntity)entity)
                .getAttribute(ForgeMod.ENTITY_GRAVITY.get())).getValue();

        net.minecraft.world.phys.Vec3 dm = entity.getDeltaMovement();

        if(entity instanceof Flingable) {
            boolean portalFling = portal.getDirection().getAxis().isVertical() && targetPortal.getDirection().getAxis().isHorizontal();
//            boolean propulsionAffected = ((IGelAffected) entity).getPropulsionTicks() > 0 && targetPortal.getDirection().getAxis().isHorizontal();

            if(portalFling) {
                ((Flingable)entity).setFlinging(true);
            } else if(targetPortal.getDirection().getAxis().isVertical()) {
                ((Flingable)entity).setFlinging(false);
            }
            // in other cases leave it alone
        }

        boolean doMathTrickery = portal.getDirection() == Direction.UP && targetPortal.getDirection() == Direction.UP
                && (!(entity instanceof Flingable) || !((Flingable)entity).isFlinging());

        if(entity instanceof IDragCancelable) {
            if(doMathTrickery) {
                if(!((IDragCancelable)entity).pmIsCancelDrag()) {
                    double yDelta = -Math.log(1 - (-delta.y) / (gravity / 0.02)) / 0.02;
                    delta = new net.minecraft.world.phys.Vec3(delta.x, ((int) yDelta) * -gravity, delta.z);
                }
    //            double yDelta = Math.sqrt(2 * gravity * entity.fallDistance);
    //            delta = new net.minecraft.world.phys.Vec3(delta.x, yDelta, delta.z);

                double deltay = Math.signum(delta.y) * Math.max(Math.abs(delta.y), 0.5);
        //        deltay = Math.floor(deltay / gravity) * gravity;
        //        deltay -= Math.signum(deltay) * (Math.abs(deltay) % gravity);
                double deltayResidual = Math.abs(deltay) % gravity;
    //            if(entity instanceof IDragCancelable && !((IDragCancelable)entity).pmIsCancelDrag())
                    deltay = Math.signum(deltay) * (Math.abs(deltay) - deltayResidual + (deltayResidual > (gravity / 2) ? gravity : 0));
    //            else
    //                deltay = Math.signum(deltay) * (Math.abs(deltay) - deltayResidual);
                delta = new net.minecraft.world.phys.Vec3(delta.x, deltay, delta.z);





                if(!((IDragCancelable)entity).pmIsCancelDrag()) {
                    double ydm = -Math.log(1 - (-dm.y) / (gravity / 0.02)) / 0.02;
                    dm = new net.minecraft.world.phys.Vec3(dm.x, ((int) ydm) * -gravity, dm.z);
                }
    //            double ydm = Math.sqrt(2 * gravity * entity.fallDistance);
    //            dm = new net.minecraft.world.phys.Vec3(dm.x, ydm, dm.z);


                double dmy = Math.signum(dm.y) * Math.max(Math.abs(dm.y), 0.5);
        //        dmy = Math.floor(dmy / gravity) * gravity;
                double dmyResidual = Math.abs(dmy) % gravity;
    //            if(entity instanceof IDragCancelable && !((IDragCancelable)entity).pmIsCancelDrag())
                    dmy = Math.signum(deltay) * (Math.abs(dmy) - dmyResidual + (deltayResidual > (gravity / 2) ? gravity : 0));
    //            else
    //                dmy = Math.signum(dmy) * (Math.abs(dmy) - dmyResidual);
                dm = new net.minecraft.world.phys.Vec3(dm.x, dmy, dm.z);



                ((IDragCancelable)entity).pmSetCancelDrag(true);
            } else {
                ((IDragCancelable)entity).pmSetCancelDrag(false);
            }
        }

        delta = portal.teleportVector(new Vec3(delta)).to3d();
        entity.setDeltaMovement(portal.teleportVector(new Vec3(dm)).to3d());
        if(entity instanceof AbstractHurtingProjectile) {
            AbstractHurtingProjectile damagingProjectile = (AbstractHurtingProjectile)entity;
            Vec3 power = new Vec3(
                    damagingProjectile.xPower,
                    damagingProjectile.yPower,
                    damagingProjectile.zPower
            );
            Vec3 teleportedPower = portal.teleportVector(power);
            damagingProjectile.xPower = teleportedPower.x;
            damagingProjectile.yPower = teleportedPower.y;
            damagingProjectile.zPower = teleportedPower.z;
        }
        OrthonormalBasis portalBasis = portal.getSourceBasis();
        OrthonormalBasis targetPortalBasis = targetPortal.getDestinationBasis();
        Mat4 changeOfBasisMatrix = portalBasis.getChangeOfBasisMatrix(targetPortalBasis);

        Vec3 center = new Vec3(entity.getBoundingBox().getCenter());
        Vec3 eyeVec = new Vec3(entity.getEyePosition(1)).sub(center);

        eyeVec.transform(changeOfBasisMatrix);

        if(entity instanceof Player && ((Player)entity).isLocalPlayer()) {
            if(portal.getDirection().getAxis().getPlane() != targetPortal.getDirection().getAxis().getPlane()) {
                CameraAnimator.getInstance().startPosAnimation(oldTeleportedCenter.clone().add(eyeVec), new Vec3(entity.getEyePosition(1)), 500);
            }

            CameraRotator.rotate(entity, portal, targetPortal);
        }

        boolean shouldDisableFlying = portal.getDirection().getAxis() == Direction.Axis.Y
                && targetPortal.getDirection().getAxis() == Direction.Axis.Y
                && portal.getDirection() == targetPortal.getDirection();

        boolean shouldStopEntity = entity instanceof Player && ((Player) entity).abilities.flying
                && portal.getDirection().getAxis().getPlane() != targetPortal.getDirection().getAxis().getPlane();

        if(entity instanceof Player && shouldDisableFlying) {
            ((Player)entity).abilities.flying = false;
        }

        if(targetPortal.getDirection() == Direction.UP && portal.getDirection() != Direction.DOWN) {
            float amount = (float) new Vec3(targetPortal.direction).dot(entity.getDeltaMovement());
            float target = 0.7f;

            if(amount < target)
                entity.setDeltaMovement(new Vec3(entity.getDeltaMovement())
                        .add(new Vec3(targetPortal.direction).mul(target - amount)).to3d());
        }

        if(shouldStopEntity) {
            entity.setDeltaMovement(new net.minecraft.world.phys.Vec3(0, 0, 0));
        }

        // todo send custom packet for this
        ((ITeleportable)entity).setLastUsedPortal(portal.getId());
        ((ITeleportable2)entity).setJustUsedPortal(targetPortal.getId());

        // todo do these only once and not recursively
        if(entity instanceof PortalHandler) {
            ((PortalHandler)entity).onTeleport(portal, targetPortal);
        }

        if(entity instanceof Player && entity.level.isClientSide) {
            float pitch = 0.9f + (float)entity.getDeltaMovement().length() / 4 * 0.4f;
            level.playSound((Player)entity, targetPortal.position().x, targetPortal.position().y, targetPortal.position().z,
                    SoundInit.PORTAL_TELEPORT.get(), SoundSource.PLAYERS, 0.5f, pitch * ModUtil.randomSlightSoundPitch());
        }

        if(justExited == null && entity instanceof Player && entity.level.isClientSide)
                // todo don't send from client instead do on server
                PacketInit.INSTANCE.sendToServer(new CPlayerPortalTeleportPacket());
        ((ITeleportLerpable)entity).setHasUsedPortal(true);

        return recursivelyTeleportEntity(entity, delta, targetPortal, depth + 1);
    }

    public static net.minecraft.world.phys.Vec3 teleportEntity(Entity entity, net.minecraft.world.phys.Vec3 delta) {
        ((ITeleportable2)entity).removeJustUsedPortal();
        return recursivelyTeleportEntity(entity, delta, null, 0);
    }

    private static boolean isFiniteVec(net.minecraft.world.phys.Vec3 v) {
        return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }

    private static boolean isFiniteAABB(AABB bb) {
        return Double.isFinite(bb.minX) && Double.isFinite(bb.minY) && Double.isFinite(bb.minZ)
                && Double.isFinite(bb.maxX) && Double.isFinite(bb.maxY) && Double.isFinite(bb.maxZ);
    }

    public static net.minecraft.world.phys.Vec3 doFunneling(Entity entity, net.minecraft.world.phys.Vec3 delta) {
        final float funnelHeight = 32;

        if(entity.level.isClientSide && !PortalModConfigManager.PORTAL_FUNNELING.get()) {
            return delta;
        }

        boolean fastEnough = delta.y < -.5;
        boolean fallingMore = Math.abs(delta.y) > Math.abs(delta.x) && Math.abs(delta.y) > Math.abs(delta.z);
        if(!fastEnough || !fallingMore)
            return delta;

        boolean moving = false;
        Supplier<Supplier<Boolean>> localPlayerMovingSupplier = () -> PortalEntityClient::isLocalPlayerMoving;

        if(entity.level.isClientSide) {
            moving = localPlayerMovingSupplier.get().get();
        }

        float downDot = (float)entity.getViewVector(1).dot(new Vec3(Direction.DOWN.getNormal()).to3d());

        if(entity instanceof Player && (downDot < .5 || moving))
            return delta;

        Vec3 entityPos = new Vec3(entity.position());
        AABB travelAABB = entity.getBoundingBox()
                .expandTowards(delta)
                .expandTowards(0, -funnelHeight, 0)
                .expandTowards(3, 0, 3)
                .expandTowards(-3, 0, -3);

        List<PortalEntity> portals = getOpenPortals(entity.level, travelAABB,
                portal -> portal.getDirection() == Direction.UP && portal.position().y < entityPos.y);

        if(portals.isEmpty())
            return delta;

        PortalEntity portal = portals.stream().reduce((portal1, portal2) -> {
            Vec3 portal1Pos = new Vec3(portal1.position());
            Vec3 portal2Pos = new Vec3(portal2.position());

            Vec3 flatEntityPos = entityPos.clone().mul(1, 0, 1);
            Vec3 flatPortal1Pos = portal1Pos.clone().mul(1, 0, 1);
            Vec3 flatPortal2Pos = portal2Pos.clone().mul(1, 0, 1);

            double hDistance1 = flatPortal1Pos.clone().sub(flatEntityPos).magnitude();
            double hDistance2 = flatPortal2Pos.clone().sub(flatEntityPos).magnitude();
            double vDistance1 = portal1Pos.y - entityPos.y;
            double vDistance2 = portal2Pos.y - entityPos.y;

            if(hDistance2 == hDistance1)
                return vDistance2 < vDistance1 ? portal2 : portal1;
            return hDistance2 < hDistance1 ? portal2 : portal1;
        }).get();

        ClipContext rayContext = new ClipContext(entity.getEyePosition(1), portal.position(), ClipContext.BlockMode.COLLIDER, ClipContext.FluidMode.ANY, entity);
        BlockHitResult rayResult = entity.level.clip(rayContext);

        // don't if there's blocks in between
        if(rayResult.getType() != HitResult.Type.MISS)
            return delta;

        Vec3 portalPos = new Vec3(portal.position());
        Vec3 relativeEntityPos = entityPos.clone().sub(portalPos.clone());
        Vec3 flatRelativeEntityPos = relativeEntityPos.clone().mul(1, 0, 1);
        float coneRadius = (float)relativeEntityPos.y * .2f;

        boolean isInCone = relativeEntityPos.y < funnelHeight && relativeEntityPos.y > 0
                && flatRelativeEntityPos.magnitude() < coneRadius;

        if(!isInCone)
            return delta;

        float currentHeight = (float)(relativeEntityPos.y);
        float startHeight = Math.min(currentHeight + entity.fallDistance, funnelHeight);
        float progress = 1 - currentHeight / startHeight;

        float distanceFactor = (float)(1 - Math.exp(-2 * progress));
        Vec3 funnelAcceleration = flatRelativeEntityPos.clone().negate().mul(distanceFactor);

        return delta.add(funnelAcceleration.to3d());
    }

    public void pushEntities() {
        AABB aabb = this.getBoundingBox();
        List<Entity> entities = this.level.getEntities((Entity)null, aabb,
                entity -> !entity.isSpectator() && !(entity instanceof PortalEntity));

        if(this.level.isClientSide) {
            entities = entities.stream()
                    .filter(entity -> entity instanceof Player && ((Player)entity).isLocalPlayer())
                    .collect(Collectors.toList());
        }

        entities.forEach(entity -> entity.setDeltaMovement(entity.getDeltaMovement()
                .add(new Vec3(this.getDirection()).mul(.2).to3d())));
    }

    public Vec3 projectPointOnPortalSurface(Vec3 point) {
        Vec3 portalToPoint = point.sub(this.getBoundingBox().getCenter());
        Vec3 n = new Vec3(this.getDirection().getNormal());
        Vec3 y = new Vec3(this.getUpVector().getNormal());
        Vec3 x = y.clone().cross(n);
        return new Vec3(portalToPoint.dot(x), portalToPoint.dot(y), 0);
    }

    public boolean arePointsAlignedToPortal(Vec3... points) {
        AABB bounds = new AABB(-WIDTH / 2, -HEIGHT / 2, -1, WIDTH / 2, HEIGHT / 2, 1);
        return Arrays.stream(points).allMatch(point ->
                bounds.contains(this.projectPointOnPortalSurface(point).to3d()));
    }

    public boolean isEntityAlignedToPortal(Entity entity) {
        return this.arePointsAlignedToPortal(new Vec3(entity.getBoundingBox().getCenter().add(entity.getDeltaMovement())));
    }

    public static boolean shouldSkipCollision(BlockGetter blockReader, BlockPos pos, CollisionContext selectionContext) {
        Entity entity = selectionContext.getEntity();
        if(entity == null || blockReader.getBlockState(pos).isAir())
            return false;

        if(((ITeleportable2)entity).hasJustUsedPortal()) {
            PortalEntity portal = (PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal());
            if(portal == null)
                return false;

            if(new Vec3(pos).add(.5).sub(portal.position()).dot(portal.getNormal()) <= 0)
                return true;
        }

        AABB travelAABB = entity.getBoundingBox().expandTowards(entity.getDeltaMovement());

        boolean noneOfTheServersBusiness = !entity.level.isClientSide && entity instanceof Player;
        AABB actualAABB = noneOfTheServersBusiness ? travelAABB.inflate(3) : travelAABB;

        List<PortalEntity> portals = getOpenPortals(entity.level, actualAABB, portal -> {
            Vec3 center = new Vec3(pos).blockCenter();
            Vec3 portalBlockCenter = new Vec3(portal.position()).blockCenter();
            boolean isBlockBehind = (float)center.clone().sub(portalBlockCenter).dot(portal.getNormal()) < 0;
            boolean isBlockSupportingPortal = portal.getBlocksBehind().contains(pos);
            boolean entityAligned = portal.isEntityAlignedToPortal(entity);
            boolean velocityAffine = entity.getDeltaMovement().dot(new net.minecraft.world.phys.Vec3(portal.getNormal())) <= 0;
            boolean intersect = entity.getBoundingBox().intersects(portal.getBoundingBox());
            boolean hasJustUsedPortal = ((ITeleportable2)entity).hasJustUsedPortal();
            boolean justUsedPortalInFront = false;

            if(hasJustUsedPortal) {
                PortalEntity justUsedPortal = (PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal());
                if(justUsedPortal != null) {
                    justUsedPortalInFront = new Vec3(portal.position()).sub(justUsedPortal.position()).dot(justUsedPortal.getNormal()) > 0;
                }
            }

            return (isBlockBehind || isBlockSupportingPortal) && entityAligned && (velocityAffine || intersect) && (!hasJustUsedPortal || justUsedPortalInFront);
        });

        return !portals.isEmpty();
    }

    public static VoxelShape getCollisionShape(Entity entity) {
        net.minecraft.world.phys.Vec3 delta = entity.getDeltaMovement();
        if(entity.isOnGround())
            delta = delta.multiply(1, 0, 1);

        AABB travelAABB = entity.getBoundingBox().expandTowards(delta);

        List<PortalEntity> portals = getOpenPortals(entity.level, travelAABB, portal -> true);

        if(portals.isEmpty())
            return Shapes.empty();

        Optional<PortalEntity> portalOptional = portals.stream().reduce((o, n) ->
            n.position().subtract(entity.position()).length() < o.position().subtract(entity.position()).length() ? n : o);

        PortalEntity portal = portalOptional.get();
        Vec3 origin = new Vec3(portal.position()).add(new Vec3(portal.getNormal()).mul(.001));
        Vec3 normal = new Vec3(portal.getDirection().getNormal());
        Vec3 right = new Vec3(portal.getRightVector().getNormal());
        Vec3 up = new Vec3(portal.getUpVector().getNormal());

        VoxelShape boundingBox = Shapes.create(new AABB(
                origin.clone()
                        .sub(normal)
                        .sub(right.clone().mul(1.5))
                        .sub(up.clone().mul(2))
                        .to3d(),
                origin.clone()
                        .sub(normal.clone().mul(0.002))
                        .add(right.clone().mul(1.5))
                        .add(up.clone().mul(2))
                        .to3d()
        ));

        VoxelShape carving = Shapes.create(new AABB(
                origin.clone()
                        .sub(normal)
                        .sub(right.clone().mul(.5))
                        .sub(up.clone().mul(1))
                        .to3d(),
                origin.clone()
                        .sub(normal.clone().mul(0.002))
                        .add(right.clone().mul(.5))
                        .add(up.clone().mul(1))
                        .to3d()
        ));

        VoxelShape shape = Shapes.join(boundingBox, carving, BooleanOp.ONLY_FIRST);
        if(PortalMod.DEBUG)
            DebugRenderer.putShape(portal.toString(), shape, Color.GREEN);

        return shape;
    }

    public List<BlockPos> getBlocksBehind() {
        AABB aabb = this.getBoundingBox().deflate(.001)
                .move(new Vec3(this.getDirection().getOpposite().getNormal()).mul(1/16f).to3d());
        return AABBUtil.getBlocksWithin(aabb);
    }

    protected void recalculateBoundingBox() {
        if(this.direction == null || this.up == null)
            return;

        Vec3 baseVertex = new Vec3(0);
        Vec3 endVertex = new Vec3(WIDTH, HEIGHT, DEPTH);

        Mat4 matrix = setupMatrix(this.direction, this.up, this.getPivotPoint());
        baseVertex.transform(matrix);
        endVertex.transform(matrix);

        this.setBoundingBox(new AABB(baseVertex.to3d(), endVertex.to3d()));
    }

    public net.minecraft.world.phys.Vec3 getPivotPoint() {
        return this.position().add(new Vec3(this.direction.getOpposite().getNormal()).mul(.501).to3d());
    }

    public static void setupMatrix(PoseStack matrix, Direction direction, Direction upVector, net.minecraft.world.phys.Vec3 center) {
        int i = direction.getStepY() * -1;
        float yRot;

        if(direction.getAxis().isHorizontal()) {
            yRot = -direction.toYRot();
        } else {
            yRot = -upVector.getOpposite().toYRot();
            if(direction.getAxisDirection() == AxisDirection.NEGATIVE)
                yRot += 180;
        }

        matrix.translate(center.x, center.y, center.z);
        matrix.mulPose(Vector3f.YP.rotationDegrees(yRot));
        matrix.mulPose(Vector3f.XP.rotationDegrees(i * 90));
        matrix.translate(-.5f, -1f, .5f);
    }

    public static Mat4 setupMatrix(Direction direction, Direction upVector, net.minecraft.world.phys.Vec3 center) {
        Mat4 matrix = Mat4.identity();

        int i = direction.getStepY() * -1;
        float yRot;

        if(direction.getAxis().isHorizontal()) {
            yRot = -direction.toYRot();
        } else {
            yRot = -upVector.getOpposite().toYRot();
            if(direction.getAxisDirection() == AxisDirection.NEGATIVE)
                yRot += 180;
        }

        matrix.translate(center.x, center.y, center.z);
        matrix.rotateDeg(Vector3f.YP, yRot);
        matrix.rotateDeg(Vector3f.XP, i * 90);
        matrix.translate(-.5f, -1f, .5f);
        return matrix;
    }

    public Vector3f getNormal() {
        return new Vec3(this.direction.getNormal()).to3f();
    }

    public float getWallAttachmentDistance(Camera camera) {
        double distance = camera.getPosition().subtract(this.position()).length();
        return (float)Math.min(0.0001 + Math.max(0.1 / 98 * (distance - 2), 0), 0.1);
    }

    public OrthonormalBasis getSourceBasis() {
        return new OrthonormalBasis(new Vec3(this.getUpVector()).cross(new Vec3(this.getDirection())), new Vec3(this.getUpVector()));
    }

    public OrthonormalBasis getDestinationBasis() {
        return new OrthonormalBasis(new Vec3(this.getUpVector()).cross(new Vec3(this.getDirection().getOpposite())), new Vec3(this.getUpVector()));
    }

    public static Mat4 getPortalToPortalRotationMatrix(PortalEntity portal, PortalEntity otherPortal) {
        OrthonormalBasis srcBasis = portal.getSourceBasis();
        OrthonormalBasis dstBasis = otherPortal.getDestinationBasis();
        return srcBasis.getChangeOfBasisMatrix(dstBasis);
    }

    public static Mat4 getPortalToPortalRotationMatrix(PortalEntity portal, PartialPortal otherPortal) {
        OrthonormalBasis srcBasis = portal.getSourceBasis();
        OrthonormalBasis dstBasis = otherPortal.getDestinationBasis();
        return srcBasis.getChangeOfBasisMatrix(dstBasis);
    }

    public static Mat4 getPortalToPortalMatrix(PortalEntity portal, PortalEntity otherPortal) {
        Vec3 thisPos = new Vec3(portal.position());
        Vec3 otherPos = new Vec3(otherPortal.position());

        return Mat4.identity()
                .mul(Mat4.createTranslation(otherPos.x, otherPos.y, otherPos.z))
                .mul(getPortalToPortalRotationMatrix(portal, otherPortal))
                .mul(Mat4.createTranslation(-thisPos.x, -thisPos.y, -thisPos.z));
    }

    public static Mat4 getPortalToPortalMatrix(PortalEntity portal, PartialPortal otherPortal) {
        Vec3 thisPos = new Vec3(portal.position());
        Vec3 otherPos = new Vec3(otherPortal.getPosition());

        return Mat4.identity()
                .mul(Mat4.createTranslation(otherPos.x, otherPos.y, otherPos.z))
                .mul(getPortalToPortalRotationMatrix(portal, otherPortal))
                .mul(Mat4.createTranslation(-thisPos.x, -thisPos.y, -thisPos.z));
    }

    public void onReplaced() {
        if(this.level.isClientSide)
            return;

        ((ServerLevel)this.level).removeEntity(this, false);
        this.getOtherPortal().ifPresent(PortalEntity::pushEntities);
    }

    public boolean isOpen() {
        if(this.level.isClientSide)
            return ClientPortalManager.getInstance().hasPartial(this.gunUUID, this.end.other());
        return PortalManager.getInstance().has(this.gunUUID, this.end.other());
    }

    public void setEnd(PortalEnd end) {
        this.end = end;
    }

    public PortalEnd getEnd() {
        return this.end;
    }

    public void setGunUUID(UUID gunUUID) {
        this.gunUUID = gunUUID;
    }

    public UUID getGunUUID() {
        return this.gunUUID;
    }

    public void setUpVector(Direction upVector) {
        this.up = upVector;
    }

    public Direction getUpVector() {
        return this.up;
    }

    public Direction getRightVector() {
        Vec3 n = new Vec3(this.getDirection().getNormal());
        Vec3 y = new Vec3(this.getUpVector().getNormal());
        Vec3 x = y.clone().cross(n);
        return Direction.fromNormal((int)x.x, (int)x.y, (int)x.z);
    }

    public void setDirection(@Nonnull Direction direction) {
        this.direction = direction;

        if(direction.getAxis().isHorizontal()) {
            this.xRot = 0.0F;
            this.yRot = (float) (this.direction.get2DDataValue() * 90);
        } else {
            this.xRot = (float) (-90 * direction.getAxisDirection().getStep());
            this.yRot = 0.0F;
        }

        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
    }

    public boolean survives() {
        if(this.level.isClientSide)
            return true;

        Mat4 toAbsolute = this.getSourceBasis().getChangeOfBasisFromCanonicalMatrix();
        VoxelShape collision = PortalPlacer.getCollision(this.level, this.direction, new Vec3(this.position()), toAbsolute, false);
        return !PortalPlacer.portalCollides(this.direction, this.getBoundingBox().deflate(0.001), collision);
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
    }

    private boolean equals(PortalEntity portal) {
        return portal.gunUUID.equals(this.gunUUID) && portal.end == this.end;
    }

    public static List<PortalEntity> getPortals(Level level, net.minecraft.world.phys.Vec3 pos, float size, Predicate<PortalEntity> predicate) {
        return getPortals(level, new AABB(pos, pos).inflate(size), predicate);
    }

    public static List<PortalEntity> getPortals(Level level, AABB bb, Predicate<PortalEntity> predicate) {
        return level.getEntitiesOfClass(PortalEntity.class, bb, predicate);
    }

    public static List<PortalEntity> getOpenPortals(Level level, AABB bb, Predicate<PortalEntity> predicate) {
        List<PortalEntity> portals = new ArrayList<>();

        Map<UUID, PortalPair> portalPairs = PortalManager.getInstance().getPortalMap();
        if(level.isClientSide)
            portalPairs = ClientPortalManager.getInstance().getPortalMap();

        List<PortalEntity> portalList = portalPairs.values().stream()
                .map(pair -> Lists.newArrayList(pair.get(PortalEnd.PRIMARY), pair.get(PortalEnd.SECONDARY)))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for(PortalEntity portal : portalList)
            if(portal != null)
                if(portal.getBoundingBox().intersects(bb) && portal.isOpen() && predicate.test(portal))
                    portals.add(portal);
        return portals;
    }

    public Optional<PortalEntity> getOtherPortal() {
        if(this.level.isClientSide)
            return Optional.ofNullable(ClientPortalManager.getInstance().get(this.gunUUID, this.end.other()));
        return Optional.ofNullable(PortalManager.getInstance().get(this.gunUUID, this.end.other()));
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        if(this.level.isClientSide)
            return;

        boolean isPortalStillUsed = PortalManager.getInstance().get(this.gunUUID, this.end) == this;

        if(!PortalManager.getInstance().unloadingChunk && isPortalStillUsed) {
            PortalManager.getInstance().remove(this.gunUUID, this);
            PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SForgetPortalPacket(this.gunUUID, this.end));

            level.playSound(null, this.position().x, this.position().y, this.position().z,
                    SoundInit.PORTAL_CLOSE.get(), SoundSource.NEUTRAL, .8f, ModUtil.randomSlightSoundPitch());
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    public void push(double x, double y, double z) {}

    public boolean hurt(DamageSource source, float amount) {
        if(this.level.isClientSide || !this.isAlive())
            return false;

        Entity attacker = source.getEntity();

        if(source instanceof EntityDamageSource && attacker instanceof LivingEntity) {
            boolean hitWithWrench = WrenchItem.hitWithWrench((LivingEntity)attacker);
            boolean isCreative = attacker instanceof Player && ((Player)attacker).isCreative();

            if(hitWithWrench || (isCreative && !this.isOpen())) {
                this.remove();
                return true;
            }
        }

        return source == DamageSource.OUT_OF_WORLD && super.hurt(source, amount);
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Vec3 getPlanePos() {
        Vec3 posToPlane = new Vec3(this.direction.getNormal()).mul(PortalEntityRenderer.OFFSET * 10);
        return new Vec3(this.position()).add(posToPlane);
    }

    public void move(MoverType moverType, net.minecraft.world.phys.Vec3 delta) {
        if(!this.level.isClientSide && !this.removed && delta.lengthSqr() > 0.0D)
            this.remove();
    }

    public float rotate(Rotation rotation) {
        return 0;
    }

    public float mirror(Mirror mirror) {
        return 0;
    }

    public void thunderHit(ServerLevel level, LightningBolt lightning) {}

    public void refreshDimensions() {}

    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    public static boolean shouldRenderBlockOverlay(Level level, BlockPos pos) {
        return getOpenPortals(level, new AABB(pos).inflate(.1),
                portal -> portal.getBlocksBehind().contains(pos)).isEmpty();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected float getEyeHeight(Pose p_213316_1_, EntityDimensions p_213316_2_) {
        return 0.0F;
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack handItem = player.getItemInHand(hand);
        if(handItem.getItem() == Items.NAME_TAG) {
            if(handItem.hasCustomHoverName()) {
                if(!player.level.isClientSide && this.isAlive()) {
                    this.setCustomName(handItem.getHoverName());
                    handItem.shrink(1);
                }

                return InteractionResult.sidedSuccess(player.level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    public void setHue(String hue) {
        this.hue = hue;
    }

    public String getColor() {
        return this.hue;
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("gunUUID", this.gunUUID);
        ItemStack item = new ItemStack(ItemInit.PORTALGUN.get());
        item.setTag(nbt);
        return item;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer
        .writeUUID(this.gunUUID)
        .writeEnum(this.end)
        .writeByte((byte)this.up.get3DDataValue())
        .writeByte((byte)this.direction.get3DDataValue())
        .writeByte(DyeColor.valueOf(this.hue.toUpperCase()).getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.gunUUID = buffer.readUUID();
        this.end = buffer.readEnum(PortalEnd.class);
        this.up = Direction.from3DDataValue(buffer.readByte());
        this.setDirection(Direction.from3DDataValue(buffer.readByte()));
        this.hue = DyeColor.byId(buffer.readByte()).getName();
        this.recalculateBoundingBox();

        ClientPortalManager.getInstance().put(this.gunUUID, this.end, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putString("end", this.end.getSerializedName());
        nbt.putUUID("gunUUID", this.gunUUID);
        nbt.putString("up", this.up.getSerializedName().toUpperCase());
        nbt.putString("facing", this.direction.getSerializedName().toUpperCase());
        nbt.putString("hue", this.hue);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        this.end = PortalEnd.valueOf(nbt.getString("end").toUpperCase());
        this.gunUUID = nbt.getUUID("gunUUID");
        this.up = Direction.valueOf(nbt.getString("up"));
        this.setDirection(Direction.valueOf(nbt.getString("facing")));
        this.hue = nbt.getString("hue");

        // up must be perpendicular to direction, this in an assumption that's used in the code
        // if that's not the case, we enforce it to avoid issues
        Direction.Axis axis = this.up.getAxis();
        if (axis == this.direction.getAxis()) {
            this.up = Direction.fromAxisAndDirection(axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X, AxisDirection.POSITIVE);
        }
    }
}