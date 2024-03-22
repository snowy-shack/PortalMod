package net.portalmod.core.math;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;

public class Vec4 {
    public double x, y, z, w;

    public Vec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4(Vec3 v) {
        this(v.x, v.y, v.z, 1);
    }

    public Vec4(Vector4f v) {
        this(v.x(), v.y(), v.z(), v.w());
    }

    public Vec4(Vector3d v) {
        this(new Vec3(v));
    }

    public Vec4(Vector3f v) {
        this(new Vec3(v));
    }

    public Vec4(Vector3i v) {
        this(new Vec3(v));
    }

    //

    public Vec4 clone() {
        return new Vec4(this.x, this.y, this.z, this.w);
    }

    public boolean equals(Object o) {
        if(!(o instanceof Vec4))
            return false;
        Vec4 v = (Vec4)o;
        return this.x == v.x && this.y == v.y && this.z == v.z && this.w == v.w;
    }
    
    //

    public Vec4 transform(Mat4 m) {
        double x = m.m00 * this.x + m.m01 * this.y + m.m02 * this.z + m.m03 * this.w;
        double y = m.m10 * this.x + m.m11 * this.y + m.m12 * this.z + m.m13 * this.w;
        double z = m.m20 * this.x + m.m21 * this.y + m.m22 * this.z + m.m23 * this.w;
        double w = m.m30 * this.x + m.m31 * this.y + m.m32 * this.z + m.m33 * this.w;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }
    
    public Vec3 toVec3() {
        return new Vec3(this.x, this.y, this.z);
    }
}