package net.portalmod.core.math;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.function.Function;

public class Vec3 {
    public double x, y, z;
    
    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(double n) {
        this(n, n, n);
    }
    
    public Vec3(Vec3 v) {
        this(v.x, v.y, v.z);
    }

    public Vec3(Vector3d v) {
        this(v.x, v.y, v.z);
    }

    public Vec3(Vector3f v) {
        this(v.x(), v.y(), v.z());
    }

    public Vec3(Vector3i v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public Vec3(Direction d) {
        this(d.getNormal().getX(), d.getNormal().getY(), d.getNormal().getZ());
    }

    // constants

    public static Vec3 origin() {
        return new Vec3(0);
    }

    public static Vec3 infinity() {
        return new Vec3(Double.POSITIVE_INFINITY);
    }

    public static Vec3 xAxis() {
        return new Vec3(1, 0, 0);
    }

    public static Vec3 yAxis() {
        return new Vec3(0, 1, 0);
    }

    public static Vec3 zAxis() {
        return new Vec3(0, 0, 1);
    }

    public static Vec3 fromAxis(Direction.Axis axis) {
        switch(axis) {
            case X: return xAxis();
            case Y: return yAxis();
            case Z: return zAxis();
            default: return new Vec3(0);
        }
    }

    // choose

    public double choose(Direction.Axis axis) {
        switch(axis) {
            case X: return this.x;
            case Y: return this.y;
            case Z: return this.z;
            default: return 0;
        }
    }

    //

    @Override
    public Vec3 clone() {
        return new Vec3(this);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Vec3))
            return false;
        Vec3 v = (Vec3)o;
        return this.x == v.x && this.y == v.y && this.z == v.z;
    }
    
    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    // add

    public Vec3 add(Vec3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vec3 add(double x, double y, double z) {
        return this.add(new Vec3(x, y, z));
    }
    
    public Vec3 add(double n) {
        return this.add(new Vec3(n));
    }

    public Vec3 add(Vector3d v) {
        return this.add(new Vec3(v));
    }

    public Vec3 add(Vector3f v) {
        return this.add(new Vec3(v));
    }

    public Vec3 add(Vector3i v) {
        return this.add(new Vec3(v));
    }

    // sub

    public Vec3 sub(Vec3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public Vec3 sub(double x, double y, double z) {
        return this.sub(new Vec3(x, y, z));
    }

    public Vec3 sub(double n) {
        return this.sub(new Vec3(n));
    }

    public Vec3 sub(Vector3d v) {
        return this.sub(new Vec3(v));
    }

    public Vec3 sub(Vector3f v) {
        return this.sub(new Vec3(v));
    }

    public Vec3 sub(Vector3i v) {
        return this.sub(new Vec3(v));
    }

    // mul

    public Vec3 mul(Vec3 v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        return this;
    }

    public Vec3 mul(double x, double y, double z) {
        return this.mul(new Vec3(x, y, z));
    }

    public Vec3 mul(double n) {
        return this.mul(new Vec3(n));
    }

    public Vec3 mul(Vector3d v) {
        return this.mul(new Vec3(v));
    }

    public Vec3 mul(Vector3f v) {
        return this.mul(new Vec3(v));
    }

    public Vec3 mul(Vector3i v) {
        return this.mul(new Vec3(v));
    }
    
    public Vec3 negate() {
        return this.mul(-1);
    }

    public Vec3 normalComplement() {
        return this.negate().add(1);
    }

    // div

    public Vec3 div(Vec3 v) {
        this.x /= v.x;
        this.y /= v.y;
        this.z /= v.z;
        return this;
    }

    public Vec3 div(double x, double y, double z) {
        return this.div(new Vec3(x, y, z));
    }

    public Vec3 div(double n) {
        return this.div(new Vec3(n));
    }

    public Vec3 div(Vector3d v) {
        return this.div(new Vec3(v));
    }

    public Vec3 div(Vector3f v) {
        return this.div(new Vec3(v));
    }

    public Vec3 div(Vector3i v) {
        return this.div(new Vec3(v));
    }

    // dot

    public double dot(Vec3 v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public double dot(double x, double y, double z) {
        return this.dot(new Vec3(x, y, z));
    }

    public double dot(Vector3d v) {
        return this.dot(new Vec3(v));
    }

    public double dot(Vector3f v) {
        return this.dot(new Vec3(v));
    }

    public double dot(Vector3i v) {
        return this.dot(new Vec3(v));
    }

    // cross

    public Vec3 cross(Vec3 v) {
        double x = this.y * v.z - this.z * v.y;
        double y = this.z * v.x - this.x * v.z;
        double z = this.x * v.y - this.y * v.x;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3 cross(double x, double y, double z) {
        return this.cross(new Vec3(x, y, z));
    }

    public Vec3 cross(Vector3d v) {
        return this.cross(new Vec3(v));
    }

    public Vec3 cross(Vector3f v) {
        return this.cross(new Vec3(v));
    }

    public Vec3 cross(Vector3i v) {
        return this.cross(new Vec3(v));
    }

    // magnitude

    public double magnitudeSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double magnitudeInv() {
        return MathHelper.fastInvSqrt(this.magnitudeSqr());
    }

    public double magnitude() {
        return Math.sqrt(this.magnitudeSqr());
    }
    
    // normalize
    
    public Vec3 normalize() {
        double magnitude = this.magnitudeInv();
        this.x *= magnitude;
        this.y *= magnitude;
        this.z *= magnitude;
        return this;
    }

    // compute

    public Vec3 compute(Function<Double, Number> f) {
        this.x = f.apply(this.x).doubleValue();
        this.y = f.apply(this.y).doubleValue();
        this.z = f.apply(this.z).doubleValue();
        return this;
    }

    // math

    public Vec3 round() {
        return this.compute(Math::round);
    }

    public Vec3 floor() {
        return this.compute(Math::floor);
    }

    public Vec3 ceil() {
        return this.compute(Math::ceil);
    }

    public Vec3 abs() {
        return this.compute(Math::abs);
    }

    public Vec3 lerp(Vec3 v, double factor) {
        this.x = this.x * (1 - factor) + v.x * factor;
        this.y = this.y * (1 - factor) + v.y * factor;
        this.z = this.z * (1 - factor) + v.z * factor;
        return this;
    }

    public Vec3 lerp(Vector3d v, double factor) {
        return this.lerp(new Vec3(v), factor);
    }

    public Vec3 lerp(Vector3f v, double factor) {
        return this.lerp(new Vec3(v), factor);
    }

    public Vec3 lerp(Vector3i v, double factor) {
        return this.lerp(new Vec3(v), factor);
    }

    // transform
    
    public Vec3 transform(Mat4 m) {
        double x = m.m00 * this.x + m.m01 * this.y + m.m02 * this.z + m.m03;
        double y = m.m10 * this.x + m.m11 * this.y + m.m12 * this.z + m.m13;
        double z = m.m20 * this.x + m.m21 * this.y + m.m22 * this.z + m.m23;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }
    
    // adapters

    public Vector3d to3d() {
        return new Vector3d(this.x, this.y, this.z);
    }
    
    public Vector3f to3f() {
        return new Vector3f((float)this.x, (float)this.y, (float)this.z);
    }
    
    public Vector3i to3i() {
        return new Vector3i(this.x, this.y, this.z);
    }

    @Nullable
    public Direction toDirection() {
        return Direction.fromNormal((int)this.x, (int)this.y, (int)this.z);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}