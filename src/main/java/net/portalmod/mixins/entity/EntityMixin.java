package net.portalmod.mixins.entity;

import net.minecraft.entity.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.goo.GooBlock;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.interfaces.IDiscontinuouslyLerpable;
import net.portalmod.core.interfaces.IDragCancelable;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
import net.portalmod.core.util.RayTraceContextWrapper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin implements ITeleportable, ITeleportable2, IDiscontinuouslyLerpable, ITeleportLerpable {
    @Shadow protected abstract Vector3d maybeBackOffFromEdge(Vector3d p_225514_1_, MoverType p_225514_2_);

    @Shadow public World level;
    @Shadow protected boolean onGround;

//    @Shadow protected abstract AxisAlignedBB getBoundingBoxForPose(Pose pose);

    @Shadow public abstract EntitySize getDimensions(Pose p_213305_1_);

    @Shadow public abstract boolean isInWater();

    @Shadow public abstract boolean updateFluidHeightAndDoFluidPushing(ITag<Fluid> p_210500_1_, double p_210500_2_);

    private int lastUsedPortal = -1;

    @Override
    public void setLastUsedPortal(int lastUsedPortal) {
        this.lastUsedPortal = lastUsedPortal;
    }

    @Override
    public int getLastUsedPortal() {
        return this.lastUsedPortal;
    }

    @Override
    public boolean hasLastUsedPortal() {
        return this.lastUsedPortal != -1;
    }

    @Override
    public void removeLastUsedPortal() {
        this.lastUsedPortal = -1;
    }

    private int justUsedPortal = -1;

    @Unique
    private final Deque<DiscontinuousLerpPos> lerpPosQueue = new ArrayDeque<>();

    @Override
    public Deque<DiscontinuousLerpPos> getLerpPosQueue() {
        return this.lerpPosQueue;
    }

    @Unique
    private final Deque<Tuple<Vector3d, Vector3d>> lerpPositions = new ArrayDeque<>();

    @Unique
    private boolean hasUsedPortal = false;

    @Override
    public Deque<Tuple<Vector3d, Vector3d>> getLerpPositions() {
        return this.lerpPositions;
    }

    @Override
    public boolean hasUsedPortal() {
        return this.hasUsedPortal;
    }

    @Override
    public void setHasUsedPortal(boolean hasUsedPortal) {
        this.hasUsedPortal = hasUsedPortal;
    }

    @Override
    public void setJustUsedPortal(int justUsedPortal) {
        this.justUsedPortal = justUsedPortal;
    }

    @Override
    public int getJustUsedPortal() {
        return this.justUsedPortal;
    }

    @Override
    public boolean hasJustUsedPortal() {
        return this.justUsedPortal != -1;
    }

    @Override
    public void removeJustUsedPortal() {
        this.justUsedPortal = -1;
    }

    // TODO this causes the player to not be able to swim
    // this may cause the player not to be able to swim, however it is required for the portals to work
    @Redirect(
                        method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;maybeBackOffFromEdge(Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/entity/MoverType;)Lnet/minecraft/util/math/vector/Vector3d;"
            )
    )
    private Vector3d pmTeleport(Entity instance, Vector3d delta, MoverType moverType) {
//        if((Entity)(Object)this instanceof AbstractMinecartEntity)
//            return this.maybeBackOffFromEdge(delta, moverType);
//        if(instance instanceof ServerPlayerEntity)
//            return this.maybeBackOffFromEdge(delta, moverType);
        Entity thiss = (Entity)(Object)this;
        return this.maybeBackOffFromEdge(PortalEntity.teleportEntity(thiss, PortalEntity.doFunneling(thiss, delta)), moverType);
    }

    @Inject(
                        method = "move",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/Entity;onGround:Z",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void pmResetDragOnLand(MoverType moverType, Vector3d delta, CallbackInfo info) {
        if(this.onGround && this instanceof IDragCancelable) {
            ((IDragCancelable)this).pmSetCancelDrag(false);
        }
    }

    @Unique
    Vector3d capturedDelta;

    @Inject(
                        method = "collide",
            at = @At("HEAD")
    )
    private void pmCaptureCollideDelta(Vector3d delta, CallbackInfoReturnable<Vector3d> info) {
        this.capturedDelta = delta;
    }

    @Redirect(
                        method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"
            )
    )
    private AxisAlignedBB pmGetSquashedHitbox(Entity entity) {
        AxisAlignedBB hitbox = entity.getBoundingBox();
        if(!(entity instanceof LivingEntity))
            return hitbox;

        AxisAlignedBB travelAABB = hitbox.expandTowards(capturedDelta);
        boolean tall = hitbox.getYsize() > hitbox.getXsize();
        boolean flinging = ((Flingable)entity).isFlinging();
        List<PortalEntity> portalsInside = PortalEntity.getPortals(entity.level, travelAABB,
                portal -> portal.getDirection().getAxis().isHorizontal() && portal.isOpen()
                        && portal.isEntityAlignedToPortal(entity));

        if(tall && flinging && !portalsInside.isEmpty()) {
            double shrink = hitbox.getYsize() / 2 - hitbox.getXsize() / 2;
            return hitbox.inflate(0, -shrink, 0);
        }

        return hitbox;
    }

    private AxisAlignedBB getBBForPoseAndPos(Pose pose, Vec3 pos) {
        EntitySize entitysize = this.getDimensions(pose);
        float radius = entitysize.width / 2.0F;
        Vector3d min = new Vector3d(pos.x - radius, pos.y, pos.z - radius);
        Vector3d max = new Vector3d(pos.x + radius, pos.y + entitysize.height, pos.z + radius);
        return new AxisAlignedBB(min, max);
    }

    @Inject(
                        method = "canEnterPose",
            at = @At("RETURN"),
            cancellable = true
    )
    private void pmCanEnterPose(Pose newPose, CallbackInfoReturnable<Boolean> info) {
        if(!info.getReturnValueZ())
            return;

        Entity thiss = (Entity)(Object)this;
        World level = thiss.level;

        Vector3d pos = thiss.position();
        if(pos.equals(ModUtil.getOldPos(thiss)))
            pos = pos.add(thiss.getDeltaMovement());

        Pose currentPose = thiss.getPose();
        AxisAlignedBB currentBB = this.getBBForPoseAndPos(currentPose, new Vec3(pos));
        AxisAlignedBB newBB = this.getBBForPoseAndPos(newPose, new Vec3(pos));
        Vec3 delta = new Vec3(newBB.getCenter()).sub(currentBB.getCenter());

        AxisAlignedBB travelAABB = currentBB.expandTowards(delta.to3d());
        List<PortalEntity> portals = PortalEntity.getOpenPortals(level, travelAABB, portal -> {
            Vec3 entityOldPos = new Vec3(currentBB.getCenter());
            Vec3 entityPos = entityOldPos.clone().add(delta);

            return portal.isEntityAlignedToPortal(thiss)
                    && !portal.canPointEnter(entityOldPos, false) && portal.canPointEnter(entityPos, false);
        });

        info.setReturnValue(portals.isEmpty());
    }

    @ModifyVariable(
                        method = "collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            )
    )
    private ReuseableStream<VoxelShape> pmAddAdditionalCollisions(ReuseableStream<VoxelShape> reuseablestream, Vector3d vector) {
        Entity thiss = (Entity)(Object)this;
        AxisAlignedBB axisalignedbb = thiss.getBoundingBox();
        
        Stream<VoxelShape> stream2 = Stream.empty();
        if(axisalignedbb.expandTowards(vector).getSize() >= 1.0E-7D) {
            AxisAlignedBB aabb = axisalignedbb.expandTowards(vector).inflate(1.0E-7D);
            stream2 = thiss.level.getEntities(thiss, aabb, entity ->
                    entity instanceof Cube &&
                    !entity.isPassenger() &&
                    axisalignedbb.minY > entity.getBoundingBox().maxY - 0.001
            ).stream().map(Entity::getBoundingBox).map(VoxelShapes::create);
        }

        return new ReuseableStream<>(Stream.concat(Stream.concat(reuseablestream.getStream(), stream2),
                Stream.of(PortalEntity.getCollisionShape(thiss))));
    }

    @Redirect(
                        method = "pick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;clip(Lnet/minecraft/util/math/RayTraceContext;)Lnet/minecraft/util/math/BlockRayTraceResult;"
            )
    )
    private BlockRayTraceResult pmPickThroughPortal(World level, RayTraceContext context) {
        List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(level,
                new Vec3(context.getFrom()), new Vec3(context.getTo()), portal -> true);

        Entity thiss = (Entity)(Object)this;
        boolean holdingSpecialItem = thiss instanceof LivingEntity
                && (WrenchItem.hitWithWrench((LivingEntity)thiss) || ((LivingEntity)thiss).getMainHandItem().getItem() instanceof PortalGun);
        PortalRenderer.getInstance().outlineRenderingPortalChain = null;

        if(!portalChain.isEmpty() && !holdingSpecialItem) {
            BlockRayTraceResult normalRay = level.clip(context);
            Vector3d positionBeforePortal = normalRay.getLocation();
            Optional<Vector3d> optionalPositionOnPortal = portalChain.get(0).getBoundingBox().clip(context.getFrom(), context.getTo());

            if(optionalPositionOnPortal.isPresent()) {
                double distanceBeforePortal = positionBeforePortal.subtract(context.getFrom()).length();
                double distanceOnPortal = optionalPositionOnPortal.get().subtract(context.getFrom()).length();

                if(distanceBeforePortal < distanceOnPortal)
                    return normalRay;
            }

            PortalRenderer.getInstance().outlineRenderingPortalChain = portalChain;
            Mat4 portalMatrix = Mat4.identity();

            for(PortalEntity portal : portalChain) {
                if(!portal.getOtherPortal().isPresent())
                    break;

                Mat4 matrix = portal.getSourceBasis().getChangeOfBasisMatrix(portal.getOtherPortal().get().getDestinationBasis());

                portalMatrix = Mat4.identity()
                        .translate(new Vec3(portal.getOtherPortal().get().position()))
                        .mul(matrix)
                        .translate(new Vec3(portal.position()).negate())
                        .mul(portalMatrix);
            }

            Vector3d to = new Vec3(context.getTo()).transform(portalMatrix).to3d();
            Vector3d from = new Vec3(context.getFrom()).transform(portalMatrix).to3d();

            PortalEntity last = portalChain.get(portalChain.size() - 1);

            Optional<Vector3d> intersection = last.getOtherPortal().get().getBoundingBox().clip(from, to);

            if(intersection.isPresent())
                from = intersection.get();

            context = new RayTraceContextWrapper(context);
            ((RayTraceContextWrapper)context).setTo(to);
            ((RayTraceContextWrapper)context).setFrom(from);
        }

        return level.clip(context);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void checkFizzlers(CallbackInfo ci) {
        if (this instanceof Fizzleable) {
            ((Fizzleable) this).checkForFizzlers((Entity) (Object) this);
        }
    }

    // Replaced
//    @Inject(at = @At(value = "HEAD"), method = "getBlockSpeedFactor()F", cancellable = true)
//    private void pmGetPropulsionGelSpeedFactor(CallbackInfoReturnable<Float> info) {
//        Entity entity = (Entity)(Object)this;
//        BlockPos pos = entity.blockPosition();
//        BlockState state = entity.level.getBlockState(pos);
//        if(state.getBlock() == BlockInit.PROPULSION_GEL.get())
//            info.setReturnValue(((PropulsionGelBlock)state.getBlock()).getSpeedFactor(pos, state, entity));
//    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void pmAddFlowingGooPhysics(CallbackInfoReturnable<Boolean> cir, double d0, boolean flag) {
        cir.setReturnValue(this.isInWater() || flag || this.updateFluidHeightAndDoFluidPushing(FluidTagInit.GOO, GooBlock.FLOW_PUSH_STRENGTH));
    }
}