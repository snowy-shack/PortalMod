package net.portalmod.common.sorted.portal;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.PMGlobals;
import net.portalmod.core.init.*;
import net.portalmod.core.interfaces.IDragCancelable;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.packet.CPlayerPortalTeleportPacket;
import net.portalmod.core.util.BlockIterator;
import net.portalmod.core.util.DebugRenderer;
import net.portalmod.mixins.accessors.EntityAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PortalEntity extends Entity implements IEntityAdditionalSpawnData {
    private final float WIDTH = 1;
    private final float HEIGHT = 2;
    private final float DEPTH = 1/16f;

    private PortalEnd end = PortalEnd.NONE;
    private Direction direction = Direction.SOUTH;
    private Direction up = Direction.UP;
    private UUID gunUUID;
    private int age = 0;
    private boolean removalScheduled;

    private String hue = "blue";
    private String primary_color = "blue";
    private String secondary_color = "orange";

    public PortalEntity(EntityType<? extends PortalEntity> type, World level) {
        super(type, level);
    }

    public PortalEntity(World level) {
        super(EntityInit.PORTAL.get(), level);
    }

    @Override
    public void tick() {
        if(this.getY() < -64.0D)
            this.outOfWorld();
        if(!level.isClientSide && this.isAlive() && (!this.survives() || this.hasMoved() || this.removalScheduled))
            this.remove();

        if(level.isClientSide) {
            PortalPhotonParticle.createLivingParticles(this);
        }

        this.age++;
    }

    @Override
    public boolean save(CompoundNBT nbt) {
        return false;
    }

    public boolean saveGlobal(CompoundNBT nbt) {
        this.removed = false;
        return super.save(nbt);
    }

    private boolean hasMoved() {
        return this.position().distanceToSqr(new Vector3d(this.xo, this.yo, this.zo)) > 0;
    }

    public int getAge() {
        return this.age;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
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

            Mat4 portalToPortalMatrix = PortalRenderer.getPortalToPortalMatrix(this, targetPartialPortal);
            return point.clone().transform(portalToPortalMatrix);
        }

        PortalEntity targetPortal = targetPortalOptional.get();
        Mat4 portalToPortalMatrix = PortalRenderer.getPortalToPortalMatrix(this, targetPortal);
        return point.clone().transform(portalToPortalMatrix);
    }

    public boolean canPointEnter(Vec3 point) {
        Optional<PortalEntity> targetPortalOptional = this.getOtherPortal();
        if(!this.isOpen() || (!targetPortalOptional.isPresent() && !ClientPortalManager.getInstance().hasPartial(this.gunUUID, this.end.other())))
            return false;

        Vec3 portalPos = new Vec3(this.position());
        Vec3 distance = point.clone().sub(portalPos);
        return distance.dot(this.getNormal()) < 0;
    }

    public boolean canEntityEnter(Entity entity, Vector3d delta) {
        Optional<PortalEntity> targetPortalOptional = this.getOtherPortal();
        if(!this.isOpen() || !targetPortalOptional.isPresent())
            return false;

        Vec3 portalPos = new Vec3(this.position());
        Vec3 entityPos = new Vec3(entity.getBoundingBox().getCenter().add(delta));
        Vec3 distance = entityPos.clone().sub(portalPos);
        return distance.dot(this.getNormal()) < 0;
    }

    /*
     * STEPS TO PROPERLY TELEPORT ENTITY:
     * 1. get all the portals:
     *    - that are open
     *    - that the entity is aligned with
     *    - for which the entity's previous center was in front of its plane
     *    - for which the entity's next center will be behind its plane
     *    - that are not behind the last portal (in the recursion) the entity came out of
     * 2. use the first portal
     * 3. get the offset between the entity's position and the entity's center
     * 4. teleport the entity's position
     * 5. reposition bounding box using 3.'s offset
     * 6. recurse
     */
    private static Vector3d recursivelyTeleportEntity(Entity entity, Vector3d delta, PortalEntity justExited, int depth) {
//        if(true)
//            return delta;

        if(depth > 100 || (!entity.level.isClientSide && entity instanceof PlayerEntity))
            return delta;

        if(!entity.isOnGround()) {
            if(delta.y > 0 && delta.y * 0.98 - 0.08 < 0) {
                delta = new Vector3d(delta.x, 0, delta.z);
                Vector3d dm = entity.getDeltaMovement();
                entity.setDeltaMovement(dm.x, 0, dm.z);
            }
        }

//        AxisAlignedBB bb = entity.getBoundingBox();

        Vec3 positionToCenterVector = new Vec3(entity.getBoundingBox().getCenter()).sub(entity.position());
//        Vec3 deltaCenterToCenter = new Vec3(delta);
//        Vec3 toNewPoseVector = new Vec3(0);

//        if(entity instanceof ClientPlayerEntity) {
////            Pose nextPose = ((IGetPose)entity).pmGetNextPose();
////            if(nextPose != entity.getPose()) {
////                AxisAlignedBB aabb = ((EntityAccessor)entity).pmGetBoundingBoxForPose(nextPose);
////                aabb = aabb.move(new Vec3(entity.position()).add(delta).to3d());
////                Vector3d center = aabb.getCenter();
////                toNewPoseVector = new Vec3(center.subtract(entity.getBoundingBox().getCenter()));
////            }
//            if(entity.isCrouching() && entity.getPose() != Pose.CROUCHING) {
//                AxisAlignedBB aabb = ((EntityAccessor)entity).pmGetBoundingBoxForPose(Pose.CROUCHING);
//                aabb = aabb.move(new Vec3(entity.position()).add(delta).to3d());
//                Vector3d center = aabb.getCenter();
//                toNewPoseVector = new Vec3(center.subtract(entity.getBoundingBox().getCenter()));
//            }
//        }

        World level = entity.level;
        Vector3d tmpDelta = ((EntityAccessor)entity).pmCollide(delta);
        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(tmpDelta);

//        Vector3d finalDelta = delta;
//        Vec3 finalToNewPoseVector = toNewPoseVector;
        List<PortalEntity> portals = getOpenPortals(level, travelAABB, portal -> {
            Vec3 entityOldPos = new Vec3(entity.getBoundingBox().getCenter());
            Vec3 entityPos = entityOldPos.clone().add(tmpDelta);
//            Vec3 entityPos = entityOldPos.clone().add(tmpDelta).add(finalToNewPoseVector);

//            Field tickField = ObfuscationReflectionHelper.findField(GameRenderer.class, "tick");
//            try {
//                int tick = (int)tickField.get(Minecraft.getInstance().gameRenderer);
//                System.out.print(tick);
//            } catch(IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }

//            System.out.print(" " + entity.level.isClientSide);
//            System.out.print(" " + portal.isEntityAlignedToPortal(entity));
//            System.out.print(" " + !portal.canPointEnter(entityOldPos));
//            System.out.print(" " + portal.canPointEnter(entityPos));
//            System.out.print(" " + justExited);
//            System.out.print(" " + (justExited == null));
//            if(justExited != null) {
//                System.out.print(" " + new Vec3(portal.position()).sub(justExited.position()).dot(justExited.getNormal()));
//                System.out.print(" " + (new Vec3(portal.position()).sub(justExited.position()).dot(justExited.getNormal()) > 0));
//                System.out.print(" " + (portal == justExited));
//            }

//            System.out.print(" " + (
//                    portal.isEntityAlignedToPortal(entity)
//                    && !portal.canPointEnter(entityOldPos) && portal.canPointEnter(entityPos)
//                    && (justExited == null
//                    || new Vec3(portal.position()).sub(justExited.position()).dot(justExited.getNormal()) > 0
//                    || portal == justExited)
//            ));

//            System.out.print("\n");

            return portal.isEntityAlignedToPortal(entity)
                    && !portal.canPointEnter(entityOldPos) && portal.canPointEnter(entityPos)
                    && (justExited == null
                        || new Vec3(portal.position()).sub(justExited.position()).dot(justExited.getNormal()) > 0
                        || portal == justExited);
        });

//        System.out.print("\n");

        if(portals.isEmpty())
            return delta;

//        Field tickField = ObfuscationReflectionHelper.findField(GameRenderer.class, "tick");
//        try {
//            int tick = (int)tickField.get(Minecraft.getInstance().gameRenderer);
//            System.out.println(tick);
//            System.out.println(portals);
//        } catch(IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }

        PortalEntity portal = portals.get(0);

//        System.out.print(" " + new Vec3(entity.position()).add(delta).add(positionToCenterVector));

//        System.out.println(entity.position());

//        Vec3 teleportedPos = portal.teleportPoint(new Vec3(entity.position()));
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

//        System.out.println(entity.position());

//        entity.getViewVector(0);










        Optional<PortalEntity> targetPortalOptional = portal.getOtherPortal();
        if(!portal.isOpen() || !targetPortalOptional.isPresent())
            return delta;

//        delta = delta.add(toNewPoseVector.to3d());

        PortalEntity targetPortal = targetPortalOptional.get();
        Vec3 portalPos = new Vec3(portal.getBoundingBox().getCenter());
        Vec3 targetPortalPos = new Vec3(targetPortal.getBoundingBox().getCenter());

        Mat4 portal1Matrix;
        Mat4 portal2Matrix;

        {
            Vector3f z1 = new Vec3(portal.getDirection().getNormal()).to3f();
            Vector3f y1 = new Vec3(portal.getUpVector().getNormal()).to3f();
            Vector3f x1 = y1.copy();
            x1.cross(z1);

            portal1Matrix = new Mat4(
                    x1.x(), x1.y(), x1.z(), 0,
                    y1.x(), y1.y(), y1.z(), 0,
                    z1.x(), z1.y(), z1.z(), 0,
                    0, 0, 0, 1
            );
        }

        {
            Vector3f z1 = new Vec3(targetPortal.getDirection().getOpposite().getNormal()).to3f();
            Vector3f y1 = new Vec3(targetPortal.getUpVector().getNormal()).to3f();
            Vector3f x1 = y1.copy();
            x1.cross(z1);

            portal2Matrix = new Mat4(
                    x1.x(), x1.y(), x1.z(), 0,
                    y1.x(), y1.y(), y1.z(), 0,
                    z1.x(), z1.y(), z1.z(), 0,
                    0, 0, 0, 1
            );
        }

        portal2Matrix.transpose();

        double gravity = 0.08;
        if(entity instanceof LivingEntity)
            gravity = Objects.requireNonNull(((LivingEntity)entity)
                .getAttribute(ForgeMod.ENTITY_GRAVITY.get())).getValue();

        Vector3d dm = entity.getDeltaMovement();

        if(entity instanceof IDragCancelable
                && portal.getDirection() == Direction.UP && targetPortal.getDirection() == Direction.UP) {

            if(!((IDragCancelable)entity).pmIsCancelDrag()) {
                double yDelta = -Math.log(1 - (-delta.y) / (gravity / 0.02)) / 0.02;
                delta = new Vector3d(delta.x, ((int) yDelta) * -gravity, delta.z);
            }
//            double yDelta = Math.sqrt(2 * gravity * entity.fallDistance);
//            delta = new Vector3d(delta.x, yDelta, delta.z);

            double deltay = Math.signum(delta.y) * Math.max(Math.abs(delta.y), 0.5);
    //        deltay = Math.floor(deltay / gravity) * gravity;
    //        deltay -= Math.signum(deltay) * (Math.abs(deltay) % gravity);
            double deltayResidual = Math.abs(deltay) % gravity;
//            if(entity instanceof IDragCancelable && !((IDragCancelable)entity).pmIsCancelDrag())
                deltay = Math.signum(deltay) * (Math.abs(deltay) - deltayResidual + (deltayResidual > (gravity / 2) ? gravity : 0));
//            else
//                deltay = Math.signum(deltay) * (Math.abs(deltay) - deltayResidual);
            delta = new Vector3d(delta.x, deltay, delta.z);





            if(!((IDragCancelable)entity).pmIsCancelDrag()) {
                double ydm = -Math.log(1 - (-dm.y) / (gravity / 0.02)) / 0.02;
                dm = new Vector3d(dm.x, ((int) ydm) * -gravity, dm.z);
            }
//            double ydm = Math.sqrt(2 * gravity * entity.fallDistance);
//            dm = new Vector3d(dm.x, ydm, dm.z);


            double dmy = Math.signum(dm.y) * Math.max(Math.abs(dm.y), 0.5);
    //        dmy = Math.floor(dmy / gravity) * gravity;
            double dmyResidual = Math.abs(dmy) % gravity;
//            if(entity instanceof IDragCancelable && !((IDragCancelable)entity).pmIsCancelDrag())
                dmy = Math.signum(deltay) * (Math.abs(dmy) - dmyResidual + (deltayResidual > (gravity / 2) ? gravity : 0));
//            else
//                dmy = Math.signum(dmy) * (Math.abs(dmy) - dmyResidual);
            dm = new Vector3d(dm.x, dmy, dm.z);



            ((IDragCancelable)entity).pmSetCancelDrag(true);
        }

        delta = new Vec3(delta)
                .transform(portal1Matrix)
                .transform(portal2Matrix)
                .to3d();

        entity.setDeltaMovement(new Vec3(dm)
                .transform(portal1Matrix)
                .transform(portal2Matrix)
                .to3d());

        OrthonormalBasis portalBasis = portal.getSourceBasis();
        OrthonormalBasis targetPortalBasis = targetPortal.getDestinationBasis();
        Mat4 changeOfBasisMatrix = portalBasis.getChangeOfBasisMatrix(targetPortalBasis);

        Vec3 center = new Vec3(entity.getBoundingBox().getCenter());
        Vec3 eyeVec = new Vec3(entity.getEyePosition(1)).sub(center);

        eyeVec.transform(changeOfBasisMatrix);

        if(portal.getDirection().getAxis().getPlane() != targetPortal.getDirection().getAxis().getPlane()) {
            CameraAnimator.getInstance().startPosAnimation(oldTeleportedCenter.clone().add(eyeVec), new Vec3(entity.getEyePosition(1)), 500);
        }

        CameraRotator.rotate(entity, portal, targetPortal);

        boolean shouldDisableFlying = portal.getDirection().getAxis().getPlane() != targetPortal.getDirection().getAxis().getPlane()
                || (portal.getDirection().getAxis() == Direction.Axis.Y
                && targetPortal.getDirection().getAxis() == Direction.Axis.Y
                && portal.getDirection() == targetPortal.getDirection());

        if(entity instanceof ClientPlayerEntity && shouldDisableFlying) {
            ((ClientPlayerEntity)entity).abilities.flying = false;
        }

        if(targetPortal.getDirection() == Direction.UP) {
            float amount = (float) new Vec3(targetPortal.direction).dot(entity.getDeltaMovement());
            float target = 0.7f;

            if(amount < target)
                entity.setDeltaMovement(new Vec3(entity.getDeltaMovement())
                        .add(new Vec3(targetPortal.direction).mul(target - amount)).to3d());
        }














//        delta = portal.teleportPoint(new Vec3(delta)).to3d();

//        System.out.print(" " + teleportedPos.clone().add(positionToCenterVector));
//        System.out.print("\n");

//        Optional<PortalEntity> targetPortalOptional = portal.getOtherPortal();
//        PortalEntity targetPortal = targetPortalOptional.get();

        // todo square aabb
        // todo send custom packet for this
        ((ITeleportable)entity).setLastUsedPortal(portal.getId());
        ((ITeleportable2)entity).setJustUsedPortal(targetPortal.getId());

        // todo do these only once and not recursively
        if(entity instanceof PortalHandler) {
            ((PortalHandler)entity).onTeleport(portal, targetPortal);
        }

        if(entity instanceof ClientPlayerEntity) {
            float pitch = 0.9f + (float)entity.getDeltaMovement().length() / 4 * 0.4f;
            level.playSound((PlayerEntity)entity, targetPortal.position().x, targetPortal.position().y, targetPortal.position().z,
                    SoundInit.PORTAL_TELEPORT.get(), SoundCategory.PLAYERS, 0.5f, pitch);
        }

        if(justExited == null && entity instanceof PlayerEntity && entity.level.isClientSide)
//            if(PortalEntityClient.hasConnection())
                // todo don't send from client instead do on server
                PacketInit.INSTANCE.sendToServer(new CPlayerPortalTeleportPacket());
        ((ITeleportLerpable)entity).setHasUsedPortal(true);

        return recursivelyTeleportEntity(entity, delta, targetPortal, depth + 1);
//        return delta;
//        return Vector3d.ZERO;
//        return delta;
    }

    public static Vector3d teleportEntity(Entity entity, Vector3d delta) {
        ((ITeleportable2)entity).removeJustUsedPortal();

//        Deque<DiscontinuousLerpPos> lerpPosQueue = ((IDiscontinuouslyLerpable)entity).getLerpPosQueue();
//        if(!lerpPosQueue.isEmpty())
//            lerpPosQueue.peekLast().extendLerp(entity.position());
//        if(recursivelyTeleportEntity(entity, delta, null) || lerpPosQueue.isEmpty())
//            lerpPosQueue.add(new DiscontinuousLerpPos(entity.position()));

        return recursivelyTeleportEntity(entity, delta, null, 0);
//        return delta;
    }

    public static Vector3d doFunneling(Entity entity, Vector3d delta) {
        if(true)
            return delta;

        if(!(entity instanceof PlayerEntity) || delta.y > -.5
                || entity.getViewVector(1).dot(new Vec3(Direction.DOWN.getNormal()).to3d()) < .9)
            return delta;

        AxisAlignedBB travelAABB = entity.getBoundingBox()
                .expandTowards(delta)
                .expandTowards(0, -10, 0)
                .expandTowards(3, 0, 3)
                .expandTowards(-3, 0, -3);

        List<PortalEntity> portals = getOpenPortals(entity.level, travelAABB,
                portal -> portal.getDirection() == Direction.UP);

        if(portals.isEmpty())
            return delta;

        Vec3 entityPos = new Vec3(entity.position());
        PortalEntity portal = portals.get(0);

        if(portals.size() > 1) {
            portal = portals.stream().reduce((portal1, portal2) -> {
                Vec3 portal1Pos = new Vec3(portal1.position());
                Vec3 portal2Pos = new Vec3(portal2.position());

                Vec3 flatEntityPos = entityPos.clone().mul(1, 0, 1);
                Vec3 flatPortal1Pos = portal1Pos.clone().mul(1, 0, 1);
                Vec3 flatPortal2Pos = portal2Pos.clone().mul(1, 0, 1);

                double hDistance1 = flatPortal1Pos.clone().sub(flatEntityPos).magnitude();
                double hDistance2 = flatPortal2Pos.clone().sub(flatEntityPos).magnitude();

                if(hDistance2 < hDistance1)
                    return portal2;

                double vDistance1 = portal1Pos.y - entityPos.y;
                double vDistance2 = portal2Pos.y - entityPos.y;

                if(hDistance2 == hDistance1 && vDistance2 < vDistance1)
                    return portal2;
                return portal1;
            }).get();
        }

        Vec3 portalPos = new Vec3(portal.getBoundingBox().getCenter());
        Vec3 relativeEntityPos = entityPos.clone().sub(portalPos.clone());
        Vec3 flatRelativeEntityPos = relativeEntityPos.clone().mul(1, 0, 1);
        float coneRadius = (float)relativeEntityPos.y * .2f;
        float posRadius = (float)flatRelativeEntityPos.magnitude();

        boolean isInCone = relativeEntityPos.y < 8 && relativeEntityPos.y > 0
                && flatRelativeEntityPos.magnitude() < coneRadius;

        if(!isInCone)
            return delta;

//        float ratio = posRadius / coneRadius;
        double angle = Math.atan2(flatRelativeEntityPos.z, flatRelativeEntityPos.x);

        Vec3 nextEntityPos = entityPos.clone().add(delta);
//        Vec3 nextRelativeEntityPos = nextEntityPos.clone().sub(portalPos.clone().add(portal.getNormal()));
//        float nextConeRadius = (float)nextRelativeEntityPos.y;
//        float nextPosRadius = nextConeRadius * ratio;

        float nextPosRadius = posRadius / 2;

        Vec3 idealNextPos = new Vec3(
                portalPos.x + Math.cos(angle) * nextPosRadius,
                nextEntityPos.y,
                portalPos.z + Math.sin(angle) * nextPosRadius
        );

        return delta.add(idealNextPos.clone().sub(nextEntityPos).mul(.5).to3d());
    }

    public static void afterEntityTick(Entity entity) {

    }
    
    private void teleportEntities() {
        if(!this.isOpen())
            return;
        
        PortalPair pair = PortalPairCache.CLIENT.get(this.gunUUID);
        if(pair == null)
            return;

        PortalEntity targetPortal = pair.get(this.end.other());
        if(targetPortal == null)
            return;
        
        Vector3f normal = new Vec3(this.direction.getNormal()).to3f();
        Vector3f normal2 = new Vec3(this.direction.getNormal()).to3f();
        normal.mul(.5f);
        normal2.mul(PortalEntityRenderer.OFFSET * 10);
        Vector3d portalPos = Vector3d.atCenterOf(this.blockPosition())
                .subtract(new Vector3d(normal)).add(new Vector3d(normal2));
        
        Vector3f targetnormal = new Vec3(targetPortal.direction.getNormal()).to3f();
        Vector3f targetnormal2 = new Vec3(targetPortal.direction.getNormal()).to3f();
        targetnormal.mul(.5f);
        targetnormal2.mul(PortalEntityRenderer.OFFSET * 10);
        Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));
        
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox());
        for(Entity entity : entities) {
            Vector3d entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vector3d distance = entityPos.subtract(portalPos);
            
            if(distance.dot(new Vec3(this.direction.getNormal()).to3d()) < 0) {
//                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                
                Vector3d move = targetPortalPos.subtract(portalPos);
                Vector3d pos = entity.position().add(move);
                entity.setPos(pos.x, pos.y, pos.z);
                entity.xo += move.x;
                entity.yo += move.y;
                entity.zo += move.z;
                entity.xOld += move.x;
                entity.yOld += move.y;
                entity.zOld += move.z;
                
                
                
//                Vector3d momentum = entity.getDeltaMovement();
//                Vector3d momentumNew = new Vector3d(momentum.x, momentum.z, -momentum.y);
//                entity.setDeltaMovement(momentumNew);
//                
//                entity.xRot -= 90;
//                entity.xRotO -= 90;
                
//                Vector3d delta = entity.getDeltaMovement();
//                Vector3d newDelta = new Vector3d(delta.x, delta.y, -delta.z);
//                entity.setDeltaMovement(newDelta);
                
//                if(!this.level.isClientSide && false) {
//                    PacketInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
//                            new EntityTeleportClientPacket(entity.getId(), move));
//                }
                
//                if(entity instanceof AbstractMinecartEntity) {
//                    System.out.println("hasdw");
//                    AbstractMinecartEntityAccessor minecart = (AbstractMinecartEntityAccessor)(AbstractMinecartEntity)entity;
//                    minecart.pmSetLX(minecart.pmGetLX() + move.x);
//                    minecart.pmSetLY(minecart.pmGetLY() + move.y);
//                    minecart.pmSetLZ(minecart.pmGetLZ() + move.z);
//                }
            }
        }
    }

    public Vec3 projectPointOnPortalSurface(Vec3 point) {
        Vec3 portalToPoint = point.sub(this.getBoundingBox().getCenter());
        Vec3 n = new Vec3(this.getDirection().getNormal());
        Vec3 y = new Vec3(this.getUpVector().getNormal());
        Vec3 x = y.clone().cross(n);
        return new Vec3(portalToPoint.dot(x), portalToPoint.dot(y), 0);
    }

    public boolean isBlockAlignedToPortal(BlockPos pos) {
        // old code:
//        for(BlockPos block : this.getBlocksBehind()) {
//            boolean result = true;
//            for(Direction.Axis axis : Direction.Axis.values()) {
//                if(axis == this.getDirection().getAxis())
//                    continue;
//                if(pos.get(axis) != block.get(axis)) {
//                    result = false;
//                    break;
//                }
//            }
//            if(result)
//                return true;
//        }
//        return false;

        return this.getBlocksBehind().stream().anyMatch(block ->
                Arrays.stream(Direction.Axis.values()).allMatch(axis ->
                        axis == this.getDirection().getAxis() || pos.get(axis) == block.get(axis)));
    }

    public boolean arePointsAlignedToPortal(Vec3... points) {
        AxisAlignedBB bounds = new AxisAlignedBB(-WIDTH / 2, -HEIGHT / 2, -1, WIDTH / 2, HEIGHT / 2, 1);
        return Arrays.stream(points).allMatch(point ->
                bounds.contains(this.projectPointOnPortalSurface(point).to3d()));
    }

    public boolean isEntityAlignedToPortal(Entity entity) {
        return this.arePointsAlignedToPortal(
//                new Vec3(entity.getBoundingBox().getCenter()),
                new Vec3(entity.getBoundingBox().getCenter().add(entity.getDeltaMovement())));
    }

    public boolean isAABBAlignedToPortal(AxisAlignedBB bb) {
        AxisAlignedBB bounds = new AxisAlignedBB(-WIDTH / 2, -HEIGHT / 2, -1, WIDTH / 2, HEIGHT / 2, 1);
        bounds = bounds.inflate(0.1);
        Vec3 min = new Vec3(bb.minX, bb.minY, bb.minZ);
        Vec3 max = new Vec3(bb.maxX, bb.maxY, bb.maxZ);
        Vec3 projectedMin = this.projectPointOnPortalSurface(min);
        Vec3 projectedMax = this.projectPointOnPortalSurface(max);
        System.out.println(projectedMin);
        System.out.println(projectedMax);
        return bounds.contains(projectedMin.to3d()) && bounds.contains(projectedMax.to3d());
//        Vec3 projected = this.projectPointOnPortalSurface(new Vec3(bb.getCenter()));
//        System.out.println(projected);
//        System.out.println(bounds.contains(projected.to3d()));
//        return bounds.contains(projected.to3d());
    }
    
    public static boolean shouldSkipCollision(IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
//        if(true)
//            return true;
//        if(pos.getY() >= 50)
//            return true;

        Entity entity = selectionContext.getEntity();
        if(entity == null || blockReader.getBlockState(pos).isAir())
            return false;

        if(((ITeleportable2)entity).hasJustUsedPortal()) {
            PortalEntity portal = (PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal());
            if(portal == null)
                return false;
//            System.out.println(((ITeleportable2)entity).getJustUsedPortal());
            if(new Vec3(pos).add(.5).sub(portal.position()).dot(portal.getNormal()) <= 0)
//            if(true)
                return true;
//            if(!currentlyTeleporting)
//                return false;
        }

//        if(((ITeleportable2)entity).hasJustUsedPortal() && !currentlyTeleporting)
//            return false;

        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(entity.getDeltaMovement());

        boolean ret = !getOpenPortals(entity.level, travelAABB, portal ->
//            portal.isBlockAlignedToPortal(pos)
            new Vec3(Vector3d.atCenterOf(pos)).sub(portal.position()).dot(portal.getNormal()) < 0
            && portal.isEntityAlignedToPortal(entity)
            && !(entity.getDeltaMovement().dot(new Vector3d(portal.getNormal())) > 0
                && !entity.getBoundingBox().intersects(portal.getBoundingBox()))
            && (!((ITeleportable2)entity).hasJustUsedPortal() ||
                    (new Vec3(portal.position())
                    .sub(entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal()).position())
                    .dot(((PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal())).getNormal()) > 0)
                )
        ).isEmpty();
//        System.out.println(entity.position());
//        System.out.println(entity.position().add(entity.getDeltaMovement()));
//        System.out.println(ret);
//        System.out.println(ret);
        return ret;
    }

    public static VoxelShape getCollisionShape(Entity entity) {
        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(entity.getDeltaMovement());

//        List<PortalEntity> portals = getOpenPortals(entity.level, travelAABB, portal -> {
//            PortalEntity justExited = (PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal());
//            return portal != justExited;
//        });

        List<PortalEntity> portals = getOpenPortals(entity.level, travelAABB, portal -> true);

        if(portals.isEmpty())
            return VoxelShapes.empty();

        Optional<PortalEntity> portalOptional = portals.stream().reduce((o, n) ->
            n.position().subtract(entity.position()).length() < o.position().subtract(entity.position()).length() ? n : o);

        PortalEntity portal = portalOptional.get();
        Vec3 origin = new Vec3(portal.position()).add(new Vec3(portal.getNormal()).mul(.001));
        Vec3 normal = new Vec3(portal.getDirection().getNormal());
        Vec3 right = new Vec3(portal.getRightVector().getNormal());
        Vec3 up = new Vec3(portal.getUpVector().getNormal());

        VoxelShape boundingBox = VoxelShapes.create(new AxisAlignedBB(
                origin.clone()
                        .sub(normal)
                        .sub(right.clone().mul(1.5))
                        .sub(up.clone().mul(2))
                        .to3d(),
                origin.clone()
                        .add(right.clone().mul(1.5))
                        .add(up.clone().mul(2))
                        .to3d()
        ));

        VoxelShape carving = VoxelShapes.create(new AxisAlignedBB(
                origin.clone()
                        .sub(normal)
                        .sub(right.clone().mul(.5))
                        .sub(up.clone().mul(1))
                        .to3d(),
                origin.clone()
                        .add(right.clone().mul(.5))
                        .add(up.clone().mul(1))
                        .to3d()
        ));

        VoxelShape shape = VoxelShapes.join(boundingBox, carving, IBooleanFunction.ONLY_FIRST);
        if(PMGlobals.DEBUG)
            DebugRenderer.putShape(portal.toString(), shape, Color.GREEN);

        return shape;
    }

    public List<BlockPos> getBlocksBehind() {
        AxisAlignedBB aabb = this.getBoundingBox().deflate(.001)
                .move(new Vec3(this.getDirection().getOpposite().getNormal()).mul(1/16f).to3d());
        return AABBUtil.getBlocksWithin(aabb);
    }

    private List<BlockState> getBlockStates(List<BlockPos> blocks) {
        return blocks.stream().map(level::getBlockState).collect(Collectors.toList());
    }

    private void updateBlocksBehind() {
        for(BlockPos pos : getBlocksBehind()) {
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 0);
        }
    }

    protected void recalculateBoundingBox() {
        if(this.direction == null || this.up == null)
            return;

        Vec3 baseVertex = new Vec3(0);
        Vec3 endVertex = new Vec3(WIDTH, HEIGHT, DEPTH);

        Mat4 matrix = setupMatrix(this.direction, this.up, this.getPivotPoint());
        baseVertex.transform(matrix);
        endVertex.transform(matrix);

        this.setBoundingBox(new AxisAlignedBB(baseVertex.to3d(), endVertex.to3d()));
    }

    public Vector3d getPivotPoint() {
        return this.position().add(new Vec3(this.direction.getOpposite().getNormal()).mul(.501).to3d());
    }

    public static void setupMatrix(MatrixStack matrix, Direction direction, Direction upVector, Vector3d center) {
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

    public static Mat4 setupMatrix(Direction direction, Direction upVector, Vector3d center) {
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

    public float getWallAttachmentDistance(ActiveRenderInfo camera) {
        double distance = camera.getPosition().subtract(this.position()).length();
        return (float)Math.min(0.0001 + Math.max(0.1 / 98 * (distance - 2), 0), 0.1);
    }

    public OrthonormalBasis getSourceBasis() {
        return new OrthonormalBasis(new Vec3(this.getUpVector()).cross(new Vec3(this.getDirection())), new Vec3(this.getUpVector()));
    }

    public OrthonormalBasis getDestinationBasis() {
        return new OrthonormalBasis(new Vec3(this.getUpVector()).cross(new Vec3(this.getDirection().getOpposite())), new Vec3(this.getUpVector()));
    }

    public void onReplaced() {
        if(!this.level.isClientSide)
            ((ServerWorld)this.level).removeEntity(this, false);
    }

    public boolean isOpen() {
//        PortalPair pair = PortalPairCache.select(this.level).get(this.gunUUID);
//        if(pair == null)
//            return false;
//        return pair.isFull();
        // todo client side too
        if(this.level.isClientSide)
            return ClientPortalManager.getInstance().hasPartial(this.gunUUID, this.end.other());
        return PortalManager.getInstance().has(this.gunUUID, this.end.other());
//                && PortalManager.get(this.gunUUID, this.end.other()).level == this.level;
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
        List<PortalEntity> portalsInside = getPortals(
                this.level,
                this.position().add(new Vec3(up.getNormal()).mul(.5f).to3d()),
                .1f,
                portal -> portal != this
                        && portal.isAlive()
                        && portal.direction == this.direction
        );

        if(!portalsInside.isEmpty())
            return false;

        boolean skipFrontBlock = new Vec3(this.position())
                .add(new Vec3(this.getNormal()).mul(1/16f - .001))
                .to3i().equals(new Vec3(this.position()).sub(new Vec3(this.getNormal()).mul(.001)).to3i());

        return AABBUtil.checkBlocksWithin(
                this.level,
                this.getBoundingBox()
                        .move(new Vec3(this.direction.getNormal()).mul(-1/16f).to3d())
                        .deflate(.001),
                (pos, state) -> PortalEntity.canSurviveOn(this.level, pos, this.direction, skipFrontBlock)
        );
    }

    public static boolean canSurviveOn(World level, BlockPos attachedBlockPos, Direction normal, boolean skipFrontBlock) {
        BlockState attachedBlock = level.getBlockState(attachedBlockPos);
        BlockState frontBlock = level.getBlockState(attachedBlockPos.relative(normal));
        BlockState behindBlock = level.getBlockState(attachedBlockPos.relative(normal.getOpposite()));

        boolean portalable = attachedBlock.is(BlockTagInit.PORTALABLE);
        boolean inheriting = attachedBlock.is(BlockTagInit.PORTAL_INHERITING);
        boolean behindPortalable = behindBlock.is(BlockTagInit.PORTALABLE);
        boolean frontNonBlocking = frontBlock.is(BlockTagInit.PORTAL_NONBLOCKING); // TODO - multiple sides definitions

        return (portalable || (inheriting && behindPortalable)) && (skipFrontBlock || frontNonBlocking);
    }

    private enum Intersection {
        UP, DOWN, BOTH, NONE
    }

    // todo adapt to unaligned
    private Intersection getIntersection(PortalEntity portal, @Nullable Direction direction, boolean skipSame) {
        if(skipSame && this.equals(portal))
            return Intersection.NONE;
        
        BlockPos pos = this.blockPosition();
        BlockPos targetPos = portal.blockPosition();
        Intersection result = Intersection.NONE;
        
        if(direction != null)
            pos = pos.relative(direction);
        
        if(portal.direction != this.direction)
            return result;
        
        if(pos.equals(targetPos) || pos.equals(targetPos.relative(portal.up)))
            result = Intersection.DOWN;
        
        pos = pos.relative(this.up);
        if(pos.equals(targetPos) || pos.equals(targetPos.relative(portal.up)))
            result = (result == Intersection.DOWN ? Intersection.BOTH : Intersection.UP);
        
        return result;
    }

    private boolean intersects(List<PortalEntity> portals) {
        for(PortalEntity portal : portals)
            if(this.getIntersection(portal, null, true) != Intersection.NONE)
                return true;
        return false;
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
    }

    // todo revise 2 methods
    private void setPos(BlockPos pos) {
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }
    
    private void movePos(Direction direction) {
        this.setPos(this.blockPosition().relative(direction));
    }
    
    private boolean equals(PortalEntity portal) {
        return portal.gunUUID.equals(this.gunUUID) && portal.end == this.end;
    }

    public boolean adjustShot(BlockRayTraceResult ray) {
        // todo use blocks behind and ahead instead
        BlockIterator interactor = new BlockIterator(this.level, this.blockPosition(), true)
        .and(direction.getOpposite(), BlockTagInit::isPortalable);
        
        if(!interactor.getResult())
            return false;
        
        List<PortalEntity> portals = getPortals(this.level, this.position(), 2, portal ->
            portal != this && portal.isAlive() && portal.getDirection() == this.direction);
        
        for(PortalEntity portal : portals) {
            switch(this.getIntersection(portal, null, true)) {
                case UP:
                    this.movePos(up.getOpposite());
                    if(this.intersects(portals))
                        return false;
                    break;
                    
                case DOWN:
                case BOTH:
                    Vector3d offset = ray.getLocation().subtract(Vector3d.atCenterOf(ray.getBlockPos()));
                    
//                    Vector3f targetLeftNormal = new Vec3(portal.direction.getNormal()).to3f();
//                    targetLeftNormal.cross(new Vec3(portal.up.getNormal()).to3f());
                    Vector3f targetLeftNormal = new Vec3(portal.direction.getNormal()).cross(new Vec3(portal.up.getNormal()).to3f()).to3f();
                    Direction targetLeft = Direction.fromNormal((int)targetLeftNormal.x(), (int)targetLeftNormal.y(), (int)targetLeftNormal.z());
                    
                    // TODO use abs
                    double x = targetLeft.getAxis().choose(offset.x, offset.y, offset.z)
                            * (targetLeft.getAxisDirection() == AxisDirection.NEGATIVE ? 1 : -1);
                    double y = portal.up.getAxis().choose(offset.x, offset.y, offset.z)
                            * (portal.up.getAxisDirection() == AxisDirection.NEGATIVE ? -1 : 1);
                    
                    boolean invert = blockPosition().equals(portal.blockPosition());
                    
                    if(invert)
                        y *= -1;
                    
                    if(y > Math.abs(x)) {
                        Direction portalUp = portal.up;
                        
                        if(invert)
                            portalUp = portalUp.getOpposite();
                        
//                        if(this.getIntersection(portal, null, true) == Intersection.BOTH) {
//                            this.setPos(this.blockPosition().relative(portalUp).relative(portalUp));
//                        } else {
//                            this.movePos(portalUp);
//                        }
                        
                        this.movePos(portalUp);
                        if(this.getIntersection(portal, null, true) != Intersection.NONE)
                            this.movePos(portalUp);
                    } else if(x >= 0) {
                        this.movePos(targetLeft.getOpposite());
                        if(this.getIntersection(portal, null, true) != Intersection.NONE)
                            this.movePos(targetLeft.getOpposite());
                    } else {
                        this.movePos(targetLeft);
                        if(this.getIntersection(portal, null, true) != Intersection.NONE)
                            this.movePos(targetLeft);
                    }
                    
                    break;
                    
                case NONE:
                    continue;
            }
            
            break;
        }
        
        BlockIterator blockPush = new BlockIterator(this.level, this.blockPosition(), false)
        .move(up)
//        .or(b -> b != Blocks.AIR)
        .or(b -> !b.is(BlockTagInit.PORTAL_NONBLOCKING))
        .move(direction.getOpposite())
        .or(b -> !BlockTagInit.isPortalable(b));
        
        if(blockPush.getResult()) {
            blockPush
            .and((pos, state, block) -> {
                this.movePos(up.getOpposite());
                return !this.intersects(portals);
            });
            
            if(!blockPush.getResult())
                return false;
        }
        
        BlockIterator blockCheck = new BlockIterator(this.level, this.blockPosition(), true)
//        .and(b -> b == Blocks.AIR)
//        .and(up, b -> b == Blocks.AIR)
        .and(b -> b.is(BlockTagInit.PORTAL_NONBLOCKING))
        .and(up, b -> b.is(BlockTagInit.PORTAL_NONBLOCKING))
        .move(direction.getOpposite())
        .and(BlockTagInit::isPortalable)
        .and(up, BlockTagInit::isPortalable)
        .and((pos, state, block) -> {
            for(PortalEntity portal : portals)
                if(this.getIntersection(portal, null, false) == Intersection.BOTH
                    && this.up == portal.up)
                    return false;
            return true;
        });
        
        return blockCheck.getResult();
    }

    public static List<PortalEntity> getPortals(World level, Vector3d pos, float size, Predicate<PortalEntity> predicate) {
        return getPortals(level, new AxisAlignedBB(pos, pos).inflate(size), predicate);
    }

    public static List<PortalEntity> getPortals(World level, AxisAlignedBB bb, Predicate<PortalEntity> predicate) {
        return level.getEntitiesOfClass(PortalEntity.class, bb, predicate);
    }

    public static List<PortalEntity> getOpenPortals(World level, AxisAlignedBB bb, Predicate<PortalEntity> predicate) {
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
//        PortalPair pair = PortalPairCache.select(this.level).get(this.gunUUID);
//        if(pair == null)
//            return Optional.empty();
//        return Optional.ofNullable(pair.get(this.end.other()));
        // todo also client
        if(this.level.isClientSide)
            return Optional.ofNullable(ClientPortalManager.getInstance().get(this.gunUUID, this.end.other()));
        return Optional.ofNullable(PortalManager.getInstance().get(this.gunUUID, this.end.other()));
    }

    public void scheduleRemoval() {
        this.removalScheduled = true;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        if(this.level.isClientSide)
            return;

//        updateBlocksBehind();
//        PortalPairCache.select(this.level).remove(gunUUID, this);
//        if(!this.level.isClientSide)
        if(!PortalManager.getInstance().unloadingChunk) {
            PortalManager.getInstance().remove(this.gunUUID, this);
            PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SForgetPortalPacket(this.gunUUID, this.end));
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    public boolean isPickable() {
        return !this.removed;
    }

    public void push(double x, double y, double z) {}

    public boolean hurt(DamageSource source, float amount) {
        return source == DamageSource.OUT_OF_WORLD && super.hurt(source, amount);
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Vec3 getPlanePos() {
        Vec3 posToPlane = new Vec3(this.direction.getNormal()).mul(PortalEntityRenderer.OFFSET * 10);
        return new Vec3(this.position()).add(posToPlane);
    }

    public void move(MoverType moverType, Vector3d delta) {
        if(!this.level.isClientSide && !this.removed && delta.lengthSqr() > 0.0D)
            this.remove();
    }

    public float rotate(Rotation rotation) {
        return 0;
    }

    public float mirror(Mirror mirror) {
        return 0;
    }

    public void thunderHit(ServerWorld level, LightningBoltEntity lightning) {}

    public void refreshDimensions() {}

    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    public static boolean shouldRenderBlockOverlay(World level, BlockPos pos) {
        return getOpenPortals(level, new AxisAlignedBB(pos).inflate(.1),
                portal -> portal.getBlocksBehind().contains(pos)).isEmpty();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected float getEyeHeight(Pose p_213316_1_, EntitySize p_213316_2_) {
        return 0.0F;
    }

    public ActionResultType interact(PlayerEntity player, Hand hand) {
        ItemStack handItem = player.getItemInHand(hand);
        if(handItem.getItem() == Items.NAME_TAG) {
            if(handItem.hasCustomHoverName()) {
                if(!player.level.isClientSide && this.isAlive()) {
                    this.setCustomName(handItem.getHoverName());
                    handItem.shrink(1);
                }

                return ActionResultType.sidedSuccess(player.level.isClientSide);
            } else {
                return ActionResultType.PASS;
            }
        }
        return ActionResultType.PASS;
    }

    public void setHue(String hue) {
        this.hue = hue;
    }
    public void setColor(String color, boolean isPrimary) {
        if (isPrimary) { this.primary_color = color;
        } else this.secondary_color = color;
        System.out.println("test");
    }

    public String getColor() {
        return this.hue;
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("gunUUID", this.gunUUID);
        ItemStack item = new ItemStack(ItemInit.PORTALGUN.get());
        item.setTag(nbt);
        return item;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer
        .writeUUID(this.gunUUID)
        .writeEnum(this.end)
//        .writeBlockPos(this.otherPortalPos)
        .writeByte((byte)this.up.get3DDataValue())
        .writeByte((byte)this.direction.get3DDataValue())
        .writeByte(DyeColor.valueOf(this.hue.toUpperCase()).getId());
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.gunUUID = buffer.readUUID();
        this.end = buffer.readEnum(PortalEnd.class);
//        this.otherPortalPos = buffer.readBlockPos();
        this.up = Direction.from3DDataValue(buffer.readByte());
        this.setDirection(Direction.from3DDataValue(buffer.readByte()));
        this.hue = DyeColor.byId(buffer.readByte()).getName();
        this.recalculateBoundingBox();

        ClientPortalManager.getInstance().put(this.gunUUID, this.end, this);

//        PortalPairCache.CLIENT.put(this.gunUUID, this.end, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putString("end", this.end.getSerializedName());
        nbt.putUUID("gunUUID", this.gunUUID);

//        CompoundNBT otherPortal = new CompoundNBT();
//        otherPortal.putInt("x", this.otherPortalPos.getX());
//        otherPortal.putInt("y", this.otherPortalPos.getY());
//        otherPortal.putInt("z", this.otherPortalPos.getZ());
//        nbt.put("otherPortal", otherPortal);

        nbt.putString("up", this.up.getSerializedName().toUpperCase());
        nbt.putString("facing", this.direction.getSerializedName().toUpperCase());
        nbt.putString("hue", this.hue);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        this.end = PortalEnd.valueOf(nbt.getString("end").toUpperCase());
        this.gunUUID = nbt.getUUID("gunUUID");

//        CompoundNBT otherPortal = (CompoundNBT)nbt.get("otherPortal");
//        int x = otherPortal.getInt("x");
//        int y = otherPortal.getInt("y");
//        int z = otherPortal.getInt("z");
//        this.otherPortalPos = new BlockPos(x, y, z);

        this.up = Direction.valueOf(nbt.getString("up"));
        this.setDirection(Direction.valueOf(nbt.getString("facing")));
        this.hue = nbt.getString("hue");
    }
}