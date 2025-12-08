package net.portalmod.core.math;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class AABBUtil {
    public static AxisAlignedBB forEachVertex(AxisAlignedBB aabb, Function<Vec3, Vec3> func) {
        Vec3 min = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        Vec3 max = new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
        return new EnhancedAABB(func.apply(min), func.apply(max));
    }

    public static AxisAlignedBB translate(AxisAlignedBB aabb, Vec3 offset) {
        return forEachVertex(aabb, vec -> vec.add(offset));
    }

    public static AxisAlignedBB scale(AxisAlignedBB aabb, Vec3 factor) {
        return forEachVertex(aabb, vec -> vec.mul(factor));
    }

    public static AxisAlignedBB transform(AxisAlignedBB aabb, Mat4 matrix) {
        return forEachVertex(aabb, vec -> vec.transform(matrix));
    }

    public static VoxelShape forEachBox(VoxelShape shape, Function<AxisAlignedBB, AxisAlignedBB> operation) {
        final VoxelShape[] result = {VoxelShapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = VoxelShapes.or(result[0], VoxelShapes.create(operation.apply(
                        new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)))));
        return result[0];
    }

    public static double getSide(AxisAlignedBB aabb, Direction normal) {
        if(normal.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            return aabb.max(normal.getAxis());
        return aabb.min(normal.getAxis());
    }

    public static List<BlockPos> getBlocksWithin(AxisAlignedBB aabb) {
        List<BlockPos> blocks = Lists.newArrayList();
        for(int z = (int)Math.floor(aabb.minZ); z <= (int)Math.floor(aabb.maxZ); z++)
            for(int y = (int)Math.floor(aabb.minY); y <= (int)Math.floor(aabb.maxY); y++)
                for(int x = (int)Math.floor(aabb.minX); x <= (int)Math.floor(aabb.maxX); x++)
                    blocks.add(new BlockPos(x, y, z));
        return blocks;
    }

    public static boolean checkBlocksWithin(World level, AxisAlignedBB aabb, BiPredicate<BlockPos, BlockState> condition) {
        for(int z = (int)Math.floor(aabb.minZ); z <= (int)Math.floor(aabb.maxZ); z++)
            for(int y = (int)Math.floor(aabb.minY); y <= (int)Math.floor(aabb.maxY); y++)
                for(int x = (int)Math.floor(aabb.minX); x <= (int)Math.floor(aabb.maxX); x++)
                    if(!condition.test(new BlockPos(x, y, z), level.getBlockState(new BlockPos(x, y, z))))
                        return false;
        return true;
    }

    public static VoxelShape addBoxesToVoxelShape(VoxelShape voxelShape, List<AxisAlignedBB> boxes) {
        for(AxisAlignedBB aabb : boxes)
            voxelShape = VoxelShapes.or(voxelShape, VoxelShapes.create(aabb));
        return voxelShape;
    }

    public static VoxelShape boxesToVoxelShape(List<AxisAlignedBB> boxes) {
        return addBoxesToVoxelShape(VoxelShapes.empty(), boxes);
    }
}