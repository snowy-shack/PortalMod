package net.portalmod.common.sorted.gel;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

public class PropulsionGelBlock extends AbstractGelBlock {
    public PropulsionGelBlock(Properties properties) {
        super(properties);
    }
    
    public float getSpeedFactor(BlockPos pos, BlockState state, Entity entity) {
        Vector3d velocity = entity.getDeltaMovement();
        
        float max = .5f;
        float x = (float)MathHelper.clamp(velocity.x, -max, max);
        float z = (float)MathHelper.clamp(velocity.z, -max, max);
        entity.setDeltaMovement(x, velocity.y, z);
        
        VoxelShape voxelshape = state.getCollisionShape(entity.level, pos, ISelectionContext.of(entity));
        VoxelShape voxelshape1 = voxelshape.move(pos.getX(), pos.getY(), pos.getZ());
        boolean flag = VoxelShapes.joinIsNotEmpty(voxelshape1, VoxelShapes.create(entity.getBoundingBox().inflate(.001f)),
                IBooleanFunction.AND);
//        if(entity instanceof IFaithPlateLaunchable && flag)
//            ((IFaithPlateLaunchable)entity).setLaunched(true);
        return flag ? 1.5f : super.getSpeedFactor();
//        return entity.isColliding(pos, state) ? 1.5f : 0;
    }
}