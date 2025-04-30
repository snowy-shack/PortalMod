package net.portalmod.common.sorted.portal;

import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

public class OrthonormalBasis {
    private Vec3 x;
    private Vec3 y;
    private Vec3 z;

    public static final OrthonormalBasis CANONICAL = new OrthonormalBasis(Vec3.xAxis(), Vec3.yAxis());

    public OrthonormalBasis(Vec3 x, Vec3 y) {
        this.x = x.clone();
        this.y = y.clone();
        this.z = x.clone().cross(y);
    }

    public Mat4 getChangeOfBasisMatrix(OrthonormalBasis destination) {
        return destination.getChangeOfBasisFromCanonicalMatrix().mul(this.getChangeOfBasisToCanonicalMatrix());
    }

    public Mat4 getChangeOfBasisToCanonicalMatrix() {
        return new Mat4(
                x.x, x.y, x.z, 0,
                y.x, y.y, y.z, 0,
                z.x, z.y, z.z, 0,
                0,   0,   0, 1
        );
    }

    public Mat4 getChangeOfBasisFromCanonicalMatrix() {
        return new Mat4(
                x.x, y.x, z.x, 0,
                x.y, y.y, z.y, 0,
                x.z, y.z, z.z, 0,
                0,   0,   0, 1
        );
    }

    public OrthonormalBasis transform(Mat4 matrix) {
        this.x = x.transform(matrix);
        this.y = y.transform(matrix);
        this.z = z.transform(matrix);
        return this;
    }

    public Vec3 getX() {
        return this.x;
    }

    public Vec3 getY() {
        return this.y;
    }

    public Vec3 getZ() {
        return this.z;
    }
}