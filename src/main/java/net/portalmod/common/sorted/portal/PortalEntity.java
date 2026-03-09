package net.portalmod.common.sorted.portal;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
import net.portalmod.PortalMod;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.core.init.*;
import net.portalmod.core.interfaces.IDragCancelable;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.packet.CPlayerPortalTeleportPacket;
import net.portalmod.core.util.DebugRenderer;
import net.portalmod.mixins.accessors.EntityAccessor;

import javax.annotation.Nonnull;
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
    private String hue = "blue";
    private int age = 0;

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
        if(!level.isClientSide && this.isAlive() && (!this.survives() || this.hasMoved()))
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

    public boolean canPointEnter(Vec3 point) {
        Optional<PortalEntity> targetPortalOptional = this.getOtherPortal();
        if(!this.isOpen() || (!targetPortalOptional.isPresent() && !ClientPortalManager.getInstance().hasPartial(this.gunUUID, this.end.other())))
            return false;

        Vec3 portalPos = new Vec3(this.position());
        Vec3 distance = point.clone().sub(portalPos);
        return distance.dot(this.getNormal()) < 0;
    }

    private static Vector3d recursivelyTeleportEntity(Entity entity, Vector3d delta, PortalEntity justExited, int depth) {
        if(depth > 100 || (!entity.level.isClientSide && entity instanceof PlayerEntity))
            return delta;

        boolean inFluid = entity.isInWater() || entity.isInLava();
        boolean flying = entity instanceof PlayerEntity && ((PlayerEntity)entity).abilities.flying;

        if(!inFluid && !entity.isOnGround() && !flying) {
            if(delta.y > 0 && delta.y * 0.98 - 0.08 < 0) {
                delta = new Vector3d(delta.x, 0, delta.z);
                Vector3d dm = entity.getDeltaMovement();
                entity.setDeltaMovement(dm.x, 0, dm.z);
            }
        }

        Vec3 positionToCenterVector = new Vec3(entity.getBoundingBox().getCenter()).sub(entity.position());

        World level = entity.level;
        Vector3d tmpDelta = ((EntityAccessor)entity).pmCollide(delta);
        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(tmpDelta);

        List<PortalEntity> portals = getOpenPortals(level, travelAABB, portal -> {
            Vec3 entityOldPos = new Vec3(entity.getBoundingBox().getCenter());
            Vec3 entityPos = entityOldPos.clone().add(tmpDelta);

            return portal.isEntityAlignedToPortal(entity)
                    && !portal.canPointEnter(entityOldPos) && portal.canPointEnter(entityPos)
                    && (justExited == null
                        || new Vec3(portal.position()).sub(justExited.position()).dot(justExited.getNormal()) > 0
                        || portal == justExited);
        });

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

        Vector3d dm = entity.getDeltaMovement();

        if(entity instanceof Flingable) {
            boolean doFling = portal.getDirection().getAxis().isVertical() && targetPortal.getDirection().getAxis().isHorizontal();
            ((Flingable)entity).setFlinging(doFling);
        }

        boolean doMathTrickery = entity instanceof IDragCancelable
                && portal.getDirection() == Direction.UP && targetPortal.getDirection() == Direction.UP
                && (!(entity instanceof Flingable) || !((Flingable)entity).isFlinging());

        if(doMathTrickery) {

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

        delta = portal.teleportVector(new Vec3(delta)).to3d();
        entity.setDeltaMovement(portal.teleportVector(new Vec3(dm)).to3d());

        OrthonormalBasis portalBasis = portal.getSourceBasis();
        OrthonormalBasis targetPortalBasis = targetPortal.getDestinationBasis();
        Mat4 changeOfBasisMatrix = portalBasis.getChangeOfBasisMatrix(targetPortalBasis);

        Vec3 center = new Vec3(entity.getBoundingBox().getCenter());
        Vec3 eyeVec = new Vec3(entity.getEyePosition(1)).sub(center);

        eyeVec.transform(changeOfBasisMatrix);

        if(entity instanceof PlayerEntity && ((PlayerEntity)entity).isLocalPlayer()) {
            if(portal.getDirection().getAxis().getPlane() != targetPortal.getDirection().getAxis().getPlane()) {
                CameraAnimator.getInstance().startPosAnimation(oldTeleportedCenter.clone().add(eyeVec), new Vec3(entity.getEyePosition(1)), 500);
            }

            CameraRotator.rotate(entity, portal, targetPortal);
        }

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
                // todo don't send from client instead do on server
                PacketInit.INSTANCE.sendToServer(new CPlayerPortalTeleportPacket());
        ((ITeleportLerpable)entity).setHasUsedPortal(true);

        return recursivelyTeleportEntity(entity, delta, targetPortal, depth + 1);
    }

    public static Vector3d teleportEntity(Entity entity, Vector3d delta) {
        ((ITeleportable2)entity).removeJustUsedPortal();
        return recursivelyTeleportEntity(entity, delta, null, 0);
    }

    public static Vector3d doFunneling(Entity entity, Vector3d delta) {
        if(!entity.level.getGameRules().getBoolean(GameRuleInit.DO_FUNNELING))
            return delta;

        float downDot = (float)entity.getViewVector(1).dot(new Vec3(Direction.DOWN.getNormal()).to3d());

        if(!(entity instanceof PlayerEntity) || delta.y > -.5 || downDot < .5)
            return delta;

        Vec3 entityPos = new Vec3(entity.position());
        AxisAlignedBB travelAABB = entity.getBoundingBox()
                .expandTowards(delta)
                .expandTowards(0, -10, 0)
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

        Vec3 portalPos = new Vec3(portal.position());
        Vec3 relativeEntityPos = entityPos.clone().sub(portalPos.clone());
        Vec3 flatRelativeEntityPos = relativeEntityPos.clone().mul(1, 0, 1);
        float coneRadius = (float)relativeEntityPos.y * .2f;

        boolean isInCone = relativeEntityPos.y < 16 && relativeEntityPos.y > 0
                && flatRelativeEntityPos.magnitude() < coneRadius;

        if(!isInCone)
            return delta;

        float speed = (float)delta.length();
        Vec3 funnelAcceleration = portalPos.clone().sub(entityPos)
                .mul(1, 0, 1)
                .mul(.8)
                .div(Math.sqrt(relativeEntityPos.y / speed));

        return delta.add(funnelAcceleration.to3d());
    }

    public void pushEntities() {
        AxisAlignedBB aabb = this.getBoundingBox();
        List<Entity> entities = this.level.getEntities((Entity)null, aabb,
                entity -> !entity.isSpectator() && !(entity instanceof PortalEntity));

        if(this.level.isClientSide) {
            entities = entities.stream()
                    .filter(entity -> entity instanceof PlayerEntity && ((PlayerEntity)entity).isLocalPlayer())
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
        AxisAlignedBB bounds = new AxisAlignedBB(-WIDTH / 2, -HEIGHT / 2, -1, WIDTH / 2, HEIGHT / 2, 1);
        return Arrays.stream(points).allMatch(point ->
                bounds.contains(this.projectPointOnPortalSurface(point).to3d()));
    }

    public boolean isEntityAlignedToPortal(Entity entity) {
        return this.arePointsAlignedToPortal(new Vec3(entity.getBoundingBox().getCenter().add(entity.getDeltaMovement())));
    }

    public static boolean shouldSkipCollision(IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
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

        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(entity.getDeltaMovement());
        AxisAlignedBB actualAABB = entity.level.isClientSide ? travelAABB : travelAABB.inflate(3);

        List<PortalEntity> portals = getOpenPortals(entity.level, actualAABB, portal -> {
            boolean blockBehind = (float)new Vec3(Vector3d.atCenterOf(pos)).sub(portal.position()).dot(portal.getNormal()) < 0;
            boolean entityAligned = portal.isEntityAlignedToPortal(entity);
            boolean velocityAffine = entity.getDeltaMovement().dot(new Vector3d(portal.getNormal())) <= 0;
            boolean intersect = entity.getBoundingBox().intersects(portal.getBoundingBox());
            boolean hasJustUsedPortal = ((ITeleportable2)entity).hasJustUsedPortal();
            boolean justUsedPortalInFront = false;

            if(hasJustUsedPortal) {
                PortalEntity justUsedPortal = (PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal());
                if(justUsedPortal != null) {
                    justUsedPortalInFront = new Vec3(portal.position()).sub(justUsedPortal.position()).dot(justUsedPortal.getNormal()) > 0;
                }
            }

            return blockBehind && entityAligned && (velocityAffine || intersect) && (!hasJustUsedPortal || justUsedPortalInFront);
        });

        return !portals.isEmpty();
    }

    public static VoxelShape getCollisionShape(Entity entity) {
        Vector3d delta = entity.getDeltaMovement();
        if(entity.isOnGround())
            delta = delta.multiply(1, 0, 1);

        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(delta);

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
                        .sub(normal.clone().mul(0.002))
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
                        .sub(normal.clone().mul(0.002))
                        .add(right.clone().mul(.5))
                        .add(up.clone().mul(1))
                        .to3d()
        ));

        VoxelShape shape = VoxelShapes.join(boundingBox, carving, IBooleanFunction.ONLY_FIRST);
        if(PortalMod.DEBUG)
            DebugRenderer.putShape(portal.toString(), shape, Color.GREEN);

        return shape;
    }

    public List<BlockPos> getBlocksBehind() {
        AxisAlignedBB aabb = this.getBoundingBox().deflate(.001)
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

        ((ServerWorld)this.level).removeEntity(this, false);
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
        VoxelShape collision = PortalPlacer.getCollision(this.level, this.direction, new Vec3(this.position()), toAbsolute);
        return !PortalPlacer.portalCollides(this.direction, this.getBoundingBox().deflate(0.001), collision);
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
    }

    private boolean equals(PortalEntity portal) {
        return portal.gunUUID.equals(this.gunUUID) && portal.end == this.end;
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
                    SoundInit.PORTAL_CLOSE.get(), SoundCategory.NEUTRAL, .8f, 1);
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
        if(this.level.isClientSide || !this.isAlive())
            return false;

        Entity attacker = source.getEntity();

        if(source instanceof EntityDamageSource && attacker instanceof LivingEntity) {
            boolean holdingWrench = WrenchItem.hitWithWrench((LivingEntity)source.getEntity());
            boolean isCreative = attacker instanceof PlayerEntity && ((PlayerEntity)attacker).isCreative();

            if(holdingWrench || isCreative) {
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
        .writeByte((byte)this.up.get3DDataValue())
        .writeByte((byte)this.direction.get3DDataValue())
        .writeByte(DyeColor.valueOf(this.hue.toUpperCase()).getId());
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.gunUUID = buffer.readUUID();
        this.end = buffer.readEnum(PortalEnd.class);
        this.up = Direction.from3DDataValue(buffer.readByte());
        this.setDirection(Direction.from3DDataValue(buffer.readByte()));
        this.hue = DyeColor.byId(buffer.readByte()).getName();
        this.recalculateBoundingBox();

        ClientPortalManager.getInstance().put(this.gunUUID, this.end, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putString("end", this.end.getSerializedName());
        nbt.putUUID("gunUUID", this.gunUUID);
        nbt.putString("up", this.up.getSerializedName().toUpperCase());
        nbt.putString("facing", this.direction.getSerializedName().toUpperCase());
        nbt.putString("hue", this.hue);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        this.end = PortalEnd.valueOf(nbt.getString("end").toUpperCase());
        this.gunUUID = nbt.getUUID("gunUUID");
        this.up = Direction.valueOf(nbt.getString("up"));
        this.setDirection(Direction.valueOf(nbt.getString("facing")));
        this.hue = nbt.getString("hue");
    }
}