package io.github.serialsniper.portalmod.mixins;

import io.github.serialsniper.portalmod.common.entities.AbstractCube;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract AxisAlignedBB getBoundingBox();
    @Shadow public World level;
    @Shadow protected boolean onGround;
    @Shadow public float maxUpStep;

//    @Shadow public abstract boolean hurt(DamageSource p_70097_1_, float p_70097_2_);

    @Inject(at = @At(value = "HEAD"), method = "collide(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", cancellable = true)
    private void portalmod_onCollide(Vector3d vector, CallbackInfoReturnable<Vector3d> info) {
        info.cancel();

        Entity thiz = (Entity)(Object)this;

        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        ISelectionContext iselectioncontext = ISelectionContext.of(thiz);
        VoxelShape voxelshape = this.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> stream = VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(axisalignedbb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
        Stream<VoxelShape> stream1 = this.level.getEntityCollisions(thiz, axisalignedbb.expandTowards(vector), entity -> true);

        Stream<VoxelShape> stream2 = Stream.empty();
        if(axisalignedbb.expandTowards(vector).getSize() >= 1.0E-7D) {
            AxisAlignedBB aabb = axisalignedbb.expandTowards(vector).inflate(1.0E-7D);
            stream2 = thiz.level.getEntities(thiz, aabb, entity -> {
                if(entity instanceof AbstractCube && thiz instanceof PlayerEntity) {
                    AxisAlignedBB cubeAABB = entity.getBoundingBox();
                    double x0 = Math.abs(cubeAABB.maxX - aabb.minX);
                    double x1 = Math.abs(cubeAABB.minX - aabb.maxX);
                    double y0 = Math.abs(cubeAABB.maxY - aabb.minY);
                    double y1 = Math.abs(cubeAABB.minY - aabb.maxY);
                    double z0 = Math.abs(cubeAABB.maxZ - aabb.minZ);
                    double z1 = Math.abs(cubeAABB.minZ - aabb.maxZ);

                    double x = Math.min(x0, x1);
                    double y = Math.min(y0, y1);
                    double z = Math.min(z0, z1);

                    return y < x && y < z;
                }

                return false;
            }).stream().map(Entity::getBoundingBox).map(VoxelShapes::create);
        }

        ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(Stream.concat(stream1, stream), stream2));
        Vector3d vector3d = vector.lengthSqr() == 0.0D ? vector : Entity.collideBoundingBoxHeuristically(thiz, vector, axisalignedbb, this.level, iselectioncontext, reuseablestream);
        boolean flag = vector.x != vector3d.x;
        boolean flag1 = vector.y != vector3d.y;
        boolean flag2 = vector.z != vector3d.z;
        boolean flag3 = this.onGround || flag1 && vector.y < 0.0D;
        if(this.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
            Vector3d vector3d1 = Entity.collideBoundingBoxHeuristically(thiz, new Vector3d(vector.x, (double)this.maxUpStep, vector.z), axisalignedbb, this.level, iselectioncontext, reuseablestream);
            Vector3d vector3d2 = Entity.collideBoundingBoxHeuristically(thiz, new Vector3d(0.0D, (double)this.maxUpStep, 0.0D), axisalignedbb.expandTowards(vector.x, 0.0D, vector.z), this.level, iselectioncontext, reuseablestream);
            if(vector3d2.y < (double)this.maxUpStep) {
                Vector3d vector3d3 = Entity.collideBoundingBoxHeuristically(thiz, new Vector3d(vector.x, 0.0D, vector.z), axisalignedbb.move(vector3d2), this.level, iselectioncontext, reuseablestream).add(vector3d2);
                if (Entity.getHorizontalDistanceSqr(vector3d3) > Entity.getHorizontalDistanceSqr(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }

            if(Entity.getHorizontalDistanceSqr(vector3d1) > Entity.getHorizontalDistanceSqr(vector3d)) {
                // return
                info.setReturnValue(vector3d1.add(Entity.collideBoundingBoxHeuristically(thiz, new Vector3d(0.0D, -vector3d1.y + vector.y, 0.0D), axisalignedbb.move(vector3d1), this.level, iselectioncontext, reuseablestream)));
            }
        }

        // return
        info.setReturnValue(vector3d);
    }
    
    protected boolean portalmod_launched = false;
    protected Vector3d portalmod_launchedPos;
    protected int portalmod_launchedTick;
    
    @Shadow protected abstract BlockPos getOnPos();
    @Shadow public abstract void setNoGravity(boolean p_189654_1_);
    @Shadow public abstract Vector3d getPosition(float p_242282_1_);
    @Shadow public int tickCount;
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateEntityAfterFallOn(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/entity/Entity;)V"), method = "move(Lnet/minecraft/entity/MoverType;Lnet/minecraft/util/math/vector/Vector3d;)V")
    private void portalmod_move(MoverType moverType, Vector3d vector, CallbackInfo info) {
        if((Entity)(Object)this instanceof ItemEntity)
            return;

        BlockPos blockpos = this.getOnPos();
        BlockState blockstate = this.level.getBlockState(blockpos);

        portalmod_launched = blockstate.getBlock() == BlockInit.FAITHPLATE.get();
        if(portalmod_launched) {
            portalmod_launchedPos = getPosition(0);
            portalmod_launchedTick = this.tickCount;
        } else {
            portalmod_launchedPos = Vector3d.ZERO;
            portalmod_launchedTick = 0;
        }
    }

//    @Shadow public abstract boolean isPassengerOfSameVehicle(Entity entity);
//
//    @Inject(at = @At(value = "HEAD"), method = "canCollideWith(Lnet/minecraft/entity/Entity;)Z", cancellable = true)
//    private void canCollideWith(Entity entity, CallbackInfoReturnable<Boolean> info) {
//        info.cancel();
////        info.setReturnValue(entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity));
//        info.setReturnValue(false);
//    }
}