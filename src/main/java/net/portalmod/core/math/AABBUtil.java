package net.portalmod.core.math;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;

public class AABBUtil {
    public static AABB forEachVertex(AABB aabb, Function<Vec3, Vec3> func) {
        Vec3 min = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        Vec3 max = new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
        return new EnhancedAABB(func.apply(min), func.apply(max));
    }

    public static AABB translate(AABB aabb, Vec3 offset) {
        return forEachVertex(aabb, vec -> vec.add(offset));
    }

    public static AABB scale(AABB aabb, Vec3 factor) {
        return forEachVertex(aabb, vec -> vec.mul(factor));
    }

    public static AABB transform(AABB aabb, Mat4 matrix) {
        return forEachVertex(aabb, vec -> vec.transform(matrix));
    }

    public static VoxelShape forEachBox(VoxelShape shape, Function<AABB, AABB> operation) {
        final VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = Shapes.joinUnoptimized(
                        result[0],
                        Shapes.create(operation.apply(new AABB(
                                minX, minY, minZ,
                                maxX, maxY, maxZ))),
                        BooleanOp.OR));
        return result[0].optimize();
    }

    public static double getSide(AABB aabb, Direction normal) {
        if(normal.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            return aabb.max(normal.getAxis());
        return aabb.min(normal.getAxis());
    }

    public static List<BlockPos> getBlocksWithin(AABB aabb) {
        List<BlockPos> blocks = Lists.newArrayList();
        for(int z = (int)Math.floor(aabb.minZ); z <= (int)Math.floor(aabb.maxZ); z++)
            for(int y = (int)Math.floor(aabb.minY); y <= (int)Math.floor(aabb.maxY); y++)
                for(int x = (int)Math.floor(aabb.minX); x <= (int)Math.floor(aabb.maxX); x++)
                    blocks.add(new BlockPos(x, y, z));
        return blocks;
    }

    public static boolean checkBlocksWithin(Level level, AABB aabb, BiPredicate<BlockPos, BlockState> condition) {
        for(int z = (int)Math.floor(aabb.minZ); z <= (int)Math.floor(aabb.maxZ); z++)
            for(int y = (int)Math.floor(aabb.minY); y <= (int)Math.floor(aabb.maxY); y++)
                for(int x = (int)Math.floor(aabb.minX); x <= (int)Math.floor(aabb.maxX); x++)
                    if(!condition.test(new BlockPos(x, y, z), level.getBlockState(new BlockPos(x, y, z))))
                        return false;
        return true;
    }

    public static VoxelShape addBoxesToVoxelShape(VoxelShape voxelShape, List<AABB> boxes) {
        for(AABB aabb : boxes)
            voxelShape = Shapes.joinUnoptimized(voxelShape, Shapes.create(aabb), BooleanOp.OR);
        return voxelShape;
    }

    public static VoxelShape boxesToVoxelShape(List<AABB> boxes) {
        return addBoxesToVoxelShape(Shapes.empty(), boxes);
    }

    public static AABB roundToTexels(AABB aabb) {
        return new AABB(
            Math.round(16 * aabb.minX) / 16.0,
            Math.round(16 * aabb.minY) / 16.0,
            Math.round(16 * aabb.minZ) / 16.0,
            Math.round(16 * aabb.maxX) / 16.0,
            Math.round(16 * aabb.maxY) / 16.0,
            Math.round(16 * aabb.maxZ) / 16.0
        );
    }
}