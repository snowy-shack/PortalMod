package net.portalmod.mixins.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.portal.DiscontinuousLerpPos;
import net.portalmod.common.sorted.portal.ITeleportable;
import net.portalmod.common.sorted.portal.ITeleportable2;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.interfaces.IDiscontinuouslyLerpable;
import net.portalmod.core.interfaces.IDragCancelable;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
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

    @Redirect(
            remap = false,
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
            remap = false,
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

    private AxisAlignedBB getBBForPoseAndPos(Pose pose, Vec3 pos) {
        EntitySize entitysize = this.getDimensions(pose);
        float radius = entitysize.width / 2.0F;
        Vector3d min = new Vector3d(pos.x - radius, pos.y, pos.z - radius);
        Vector3d max = new Vector3d(pos.x + radius, pos.y + entitysize.height, pos.z + radius);
        return new AxisAlignedBB(min, max);
    }

    @Inject(
            remap = false,
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
                    && !portal.canPointEnter(entityOldPos) && portal.canPointEnter(entityPos);
        });

        info.setReturnValue(portals.isEmpty());
    }

    @ModifyVariable(
            remap = false,
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
    
    @Inject(remap = false, at = @At(value = "HEAD"), method = "getBlockSpeedFactor()F", cancellable = true)
    private void pmGetPropulsionGelSpeedFactor(CallbackInfoReturnable<Float> info) {
        Entity entity = (Entity)(Object)this;
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level.getBlockState(pos);
        if(state.getBlock() == BlockInit.PROPULSION_GEL.get())
            info.setReturnValue(((PropulsionGelBlock)state.getBlock()).getSpeedFactor(pos, state, entity));
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, remap = false)
    public void pmAddFlowingGooPhysics(CallbackInfoReturnable<Boolean> cir, double d0, boolean flag) {
        cir.setReturnValue(this.isInWater() || flag || this.updateFluidHeightAndDoFluidPushing(FluidTagInit.GOO, 0.01));
    }
}