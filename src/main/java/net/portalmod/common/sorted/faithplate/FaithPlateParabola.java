package net.portalmod.common.sorted.faithplate;

import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.core.math.Vec3;

public class FaithPlateParabola {
    private static final double GRAVITY = 0.08;
    private final Vec3 target;
    private final Vec3 projectedTarget;
    private final double minHeight;
    private double height;
    
    public FaithPlateParabola(Vec3 target, double height) {
        this.target = target;
        this.projectedTarget = this.getProjectedTarget();
        this.minHeight = this.getMinHeight(this.getProjectedTarget());
        this.setHeight(height);
    }
    
    public FaithPlateParabola(Vector3d target, double height) {
        this(new Vec3(target), height);
    }
    
    public FaithPlateParabola(Vec3 target) {
        this(target, Double.NEGATIVE_INFINITY);
    }
    
    public FaithPlateParabola(Vector3d target) {
        this(target, Double.NEGATIVE_INFINITY);
    }
    
    public void setHeight(double height) {
        this.height = Math.max(height, this.minHeight);
    }
    
    public double getHeight() {
        return this.height;
    }
    
    private double getMinHeight(Vec3 target) {
        if(target.y == 0 || isVertical())
            return .5;
        
        Vec3 vertex = target.y > 0 ? target : new Vec3(0);
        Vec3 point = target.y < 0 ? target : new Vec3(0);
        
        double xb = target.x / 2d;
        double a = (vertex.y - point.y) / (2d * vertex.x * point.x - point.x * point.x - vertex.x * vertex.x);
        double b = -2d * a * vertex.x;
        
        return a * xb * xb + b * xb;
    }
    
    public Vec3 getProjectedTarget() {
        return new Vec3(Math.sqrt(target.x * target.x + target.z * target.z), target.y, 0);
    }
    
    public double getA() {
        return (projectedTarget.y - getB() * projectedTarget.x) / (projectedTarget.x * projectedTarget.x);
    }
    
    public double getB() {
        return (4 * height - projectedTarget.y) / projectedTarget.x;
    }
    
    public boolean isVertical() {
//        System.out.println(getA());
        return Double.isInfinite(getA()) || Double.isNaN(getA());
    }
    
    public double getAngle() {
        if(isVertical())
            return Math.PI / 2;
        return Math.atan(getB());
    }
    
    public double getVelocity() {
        if(isVertical())
            return Math.sqrt(2 * GRAVITY * height);
        return Math.sqrt(-(GRAVITY / (2d * getA()))) / Math.cos(getAngle());
    }
    
    public double getRotation() {
        if(isVertical())
            return 0;
        return Math.atan2(target.z, target.x);
    }
    
    public double getComponentX() {
        return Math.cos(getRotation());
    }
    
    public double getComponentZ() {
        return Math.sin(getRotation());
    }
    
    public double getMiddlePoint() {
        return projectedTarget.x / 2;
    }
}