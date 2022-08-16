package io.github.serialsniper.portalmod.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockMixin {
	
    @Inject(at = @At(value = "RETURN"), method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;", cancellable = true)
    private void portalmod_getCollisionShape(IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext, CallbackInfoReturnable<VoxelShape> info) {
        Entity entity = selectionContext.getEntity();

        if(entity == null)
            return;

        AxisAlignedBB aabb = new AxisAlignedBB(
                pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2,
                pos.getX() + 3, pos.getY() + 3, pos.getZ() + 3
        );

        List<PortalEntity> portals;

        try {
            portals = entity.level.getEntitiesOfClass(PortalEntity.class, aabb, portal -> {
                if(!portal.isOpen())
                    return false;
                
                BlockPos portalPos = portal.getPos();
                Direction.Axis axis = portal.getDirection().getAxis();
                boolean result = true;

                if (axis != Direction.Axis.X)
                    result &= portalPos.getX() == pos.getX();
                if (axis != Direction.Axis.Y)
                    result &= portalPos.getY() == pos.getY() || portalPos.above().getY() == pos.getY();
                if (axis != Direction.Axis.Z)
                    result &= portalPos.getZ() == pos.getZ();

                if (portal.getDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE)
                    result &= portalPos.get(axis) > pos.get(axis);
                else
                    result &= portalPos.get(axis) < pos.get(axis);

                return result;
            });

            if(portals.isEmpty())
                return;

            portals = entity.level.getEntitiesOfClass(PortalEntity.class, entity.getBoundingBox(), portal -> true);

            if(portals.isEmpty())
                return;

            PortalEntity portal = portals.get(0);
            VoxelShape base = VoxelShapes.block();
            VoxelShape left = portalmod_moveVoxelShape(base, portal.getDirection().getClockWise(), 1);
            VoxelShape right = portalmod_moveVoxelShape(base, portal.getDirection().getCounterClockWise(), 1);
            VoxelShape shape = VoxelShapes.or(left, right);

            if(portal.getPos().equals(pos.relative(portal.getDirection()))) {
                // bottom
                VoxelShape top = portalmod_moveVoxelShape(base, Direction.DOWN, 1);
                shape = VoxelShapes.or(shape, top);
            } else if(portal.getPos().equals(pos.relative(portal.getDirection()).below())) {
                // top
                VoxelShape bottom = portalmod_moveVoxelShape(base, Direction.UP, 1);
                shape = VoxelShapes.or(shape, bottom);
            } else {
                return;
            }

            info.setReturnValue(shape);
        } catch(Exception e) {
            e.printStackTrace();
        }

//        VoxelShape shape;
//
//        VoxelShape base = VoxelShapes.block().move(pos.getX(), pos.getY(), pos.getZ());
//        VoxelShape bottom = moveVoxelShape(base, Direction.DOWN, 1);
//        VoxelShape top = moveVoxelShape(base, Direction.UP, 2);
//        VoxelShape left1 = moveVoxelShape(base, );

//        info.setReturnValue(VoxelShapes.empty());
    }
    
    private VoxelShape portalmod_moveVoxelShape(VoxelShape shape, Direction direction, int multiplier) {
        Vector3i normal = direction.getNormal();
        return shape.move(normal.getX() * multiplier, normal.getY() * multiplier, normal.getZ() * multiplier);
    }
}