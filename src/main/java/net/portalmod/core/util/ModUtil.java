package net.portalmod.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public class ModUtil {
    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction, int multiplier) {
        Vector3i normal = direction.getNormal();
        return shape.move(normal.getX() * multiplier, normal.getY() * multiplier, normal.getZ() * multiplier);
    }
    
    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction) {
        return moveVoxelShape(shape, direction, 1);
    }
    
    public static BlockRayTraceResult rayTraceBlock(PlayerEntity player, World level, int length) {
        Vector3d rayPath = player.getViewVector(0).scale(length);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
        return level.clip(rayCtx);
    }

    public static Vector3d getOldPos(Entity entity) {
        return new Vector3d(entity.xo, entity.yo, entity.zo);
    }
}