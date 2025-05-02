package net.portalmod.core.math;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.function.Function;

public class EnhancedAABB extends AxisAlignedBB {
    public EnhancedAABB(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
        super(xMin, yMin, zMin, xMax, yMax, zMax);
    }

    public EnhancedAABB(BlockPos blockPos) {
        super(blockPos);
    }

    public EnhancedAABB(BlockPos blockPos1, BlockPos blockPos2) {
        super(blockPos1, blockPos2);
    }

    public EnhancedAABB(Vec3 vec1, Vec3 vec2) {
        super(vec1.to3d(), vec2.to3d());
    }

    public EnhancedAABB forEachVertex(Function<Vec3, Vec3> func) {
        Vec3 min = new Vec3(this.minX, this.minY, this.minZ);
        Vec3 max = new Vec3(this.maxX, this.maxY, this.maxZ);
        return new EnhancedAABB(func.apply(min), func.apply(max));
    }

    public EnhancedAABB translate(Vec3 offset) {
        return this.forEachVertex(vec -> vec.add(offset));
    }

    public EnhancedAABB scale(Vec3 factor) {
        return this.forEachVertex(vec -> vec.mul(factor));
    }

    public EnhancedAABB transform(Mat4 matrix) {
        return this.forEachVertex(vec -> vec.transform(matrix));
    }
}