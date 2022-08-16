package io.github.serialsniper.portalmod.core.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class FaithPlateParabola {
    private final Vector3d target;
    private final Vector3d rotatedTarget;
    private final double height;

    public FaithPlateParabola(Vector3d target, double height) {
        this.target = target;
        this.rotatedTarget = new Vector3d(Math.sqrt(this.target.x * this.target.x + this.target.z * this.target.z), this.target.y, 0);
        this.height = Math.max(height, getMinHeight(getRotatedTarget()));
    }

    public FaithPlateParabola(BlockPos target, Direction face, double height) {
        Vector3i hitNormal = face.getNormal();
        Vector3d hitNormalDouble = new Vector3d(hitNormal.getX() * .5, hitNormal.getY() * .5, hitNormal.getZ() * .5);
        this.target = new Vector3d(target.getX(), target.getY(), target.getZ()).add(.5, .5, .5).add(hitNormalDouble);
        this.rotatedTarget = new Vector3d(Math.sqrt(this.target.x * this.target.x + this.target.z * this.target.z), this.target.y, 0);
        this.height = Math.max(height, getMinHeight(getRotatedTarget()));
    }

    public double getMinHeight(Vector3d target) {
        if(target.y == 0)
            return .5;

        Vector3d vertex = target.y > 0 ? target : Vector3d.ZERO;
        Vector3d point = target.y < 0 ? target : Vector3d.ZERO;

        double xb = target.x / 2d;
        double a = (vertex.y - point.y) / (2d * vertex.x * point.x - point.x * point.x - vertex.x * vertex.x);
        double b = -2d * a * vertex.x;

        return a * xb * xb + b * xb;
    }

    public Vector3d getRotatedTarget() {
        return new Vector3d(Math.sqrt(target.x * target.x + target.z * target.z), target.y, 0);
    }

    public double getA() {
        return (rotatedTarget.y - getB() * rotatedTarget.x) / (rotatedTarget.x * rotatedTarget.x);
    }

    public double getB() {
        return (4d * height - rotatedTarget.y) / rotatedTarget.x;
    }

    public double getAngle() {
        return Math.atan(getB());
    }

    public double getVelocity() {
        return Math.sqrt(-(0.08 / (2d * getA()))) / Math.cos(getAngle());
    }

    public double getRotation() {
        return Math.atan2(target.z, target.x);
    }

    public double getComponentX() {
        return Math.cos(getRotation());
    }

    public double getComponentZ() {
        return Math.sin(getRotation());
    }
}