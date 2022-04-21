package io.github.serialsniper.portalmod.core.util;

import net.minecraft.util.math.vector.Vector3d;

public class FaithPlateParabola {
    private final double a, b, angle, velocity, rotation;

    public FaithPlateParabola(Vector3d target, double height) {
        Vector3d rotatedTarget = new Vector3d(Math.sqrt(target.x * target.x + target.z * target.z), target.y, 0);

        height = Math.max(height, getMinHeight(rotatedTarget));
        this.b = (4d * height - rotatedTarget.y) / rotatedTarget.x;
        this.a = (rotatedTarget.y - b * rotatedTarget.x) / (rotatedTarget.x * rotatedTarget.x);
        this.angle = Math.atan(b);
        this.velocity = Math.sqrt(-(0.08 / (2d * a))) / Math.cos(angle);
        this.rotation = Math.atan2(target.z, target.x);
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

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getAngle() {
        return angle;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getRotation() {
        return rotation;
    }

    public double getComponentX() {
        return Math.cos(rotation);
    }

    public double getComponentZ() {
        return Math.sin(rotation);
    }
}