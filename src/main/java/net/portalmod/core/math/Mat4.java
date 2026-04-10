package net.portalmod.core.math;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.nio.FloatBuffer;

public class Mat4 {
    public static Mat4 identity() {
        return new Mat4(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    protected double m00, m01, m02, m03;
    protected double m10, m11, m12, m13;
    protected double m20, m21, m22, m23;
    protected double m30, m31, m32, m33;

    public Mat4(double m00, double m01, double m02, double m03,
                double m10, double m11, double m12, double m13,
                double m20, double m21, double m22, double m23,
                double m30, double m31, double m32, double m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Mat4(double[] array) {
        this.m00 = array[0];
        this.m01 = array[1];
        this.m02 = array[2];
        this.m03 = array[3];
        this.m10 = array[4];
        this.m11 = array[5];
        this.m12 = array[6];
        this.m13 = array[7];
        this.m20 = array[8];
        this.m21 = array[9];
        this.m22 = array[10];
        this.m23 = array[11];
        this.m30 = array[12];
        this.m31 = array[13];
        this.m32 = array[14];
        this.m33 = array[15];
    }

    public Mat4(Quaternion q) {
       float f = q.i();
       float f1 = q.j();
       float f2 = q.k();
       float f3 = q.r();
       float f4 = 2.0F * f * f;
       float f5 = 2.0F * f1 * f1;
       float f6 = 2.0F * f2 * f2;
       this.m00 = 1.0F - f5 - f6;
       this.m11 = 1.0F - f6 - f4;
       this.m22 = 1.0F - f4 - f5;
       this.m33 = 1.0F;
       float f7 = f * f1;
       float f8 = f1 * f2;
       float f9 = f2 * f;
       float f10 = f * f3;
       float f11 = f1 * f3;
       float f12 = f2 * f3;
       this.m10 = 2.0F * (f7 + f12);
       this.m01 = 2.0F * (f7 - f12);
       this.m20 = 2.0F * (f9 - f11);
       this.m02 = 2.0F * (f9 + f11);
       this.m21 = 2.0F * (f8 + f10);
       this.m12 = 2.0F * (f8 - f10);
    }

    private static final FloatBuffer temp = FloatBuffer.allocate(16);

    public Mat4(Matrix4f m) {
        m.store(temp);
        this.m00 = temp.get(0);
        this.m01 = temp.get(1);
        this.m02 = temp.get(2);
        this.m03 = temp.get(3);
        this.m10 = temp.get(4);
        this.m11 = temp.get(5);
        this.m12 = temp.get(6);
        this.m13 = temp.get(7);
        this.m20 = temp.get(8);
        this.m21 = temp.get(9);
        this.m22 = temp.get(10);
        this.m23 = temp.get(11);
        this.m30 = temp.get(12);
        this.m31 = temp.get(13);
        this.m32 = temp.get(14);
        this.m33 = temp.get(15);
        this.transpose();
    }

    public void store(FloatBuffer buffer) {
        buffer.put(0, (float)this.m00);
        buffer.put(1, (float)this.m01);
        buffer.put(2, (float)this.m02);
        buffer.put(3, (float)this.m03);
        buffer.put(4, (float)this.m10);
        buffer.put(5, (float)this.m11);
        buffer.put(6, (float)this.m12);
        buffer.put(7, (float)this.m13);
        buffer.put(8, (float)this.m20);
        buffer.put(9, (float)this.m21);
        buffer.put(10, (float)this.m22);
        buffer.put(11, (float)this.m23);
        buffer.put(12, (float)this.m30);
        buffer.put(13, (float)this.m31);
        buffer.put(14, (float)this.m32);
        buffer.put(15, (float)this.m33);
    }

    public double adjugateAndDet() {
        double f = this.m00 * this.m11 - this.m01 * this.m10;
        double f1 = this.m00 * this.m12 - this.m02 * this.m10;
        double f2 = this.m00 * this.m13 - this.m03 * this.m10;
        double f3 = this.m01 * this.m12 - this.m02 * this.m11;
        double f4 = this.m01 * this.m13 - this.m03 * this.m11;
        double f5 = this.m02 * this.m13 - this.m03 * this.m12;
        double f6 = this.m20 * this.m31 - this.m21 * this.m30;
        double f7 = this.m20 * this.m32 - this.m22 * this.m30;
        double f8 = this.m20 * this.m33 - this.m23 * this.m30;
        double f9 = this.m21 * this.m32 - this.m22 * this.m31;
        double f10 = this.m21 * this.m33 - this.m23 * this.m31;
        double f11 = this.m22 * this.m33 - this.m23 * this.m32;
        double f12 = this.m11 * f11 - this.m12 * f10 + this.m13 * f9;
        double f13 = -this.m10 * f11 + this.m12 * f8 - this.m13 * f7;
        double f14 = this.m10 * f10 - this.m11 * f8 + this.m13 * f6;
        double f15 = -this.m10 * f9 + this.m11 * f7 - this.m12 * f6;
        double f16 = -this.m01 * f11 + this.m02 * f10 - this.m03 * f9;
        double f17 = this.m00 * f11 - this.m02 * f8 + this.m03 * f7;
        double f18 = -this.m00 * f10 + this.m01 * f8 - this.m03 * f6;
        double f19 = this.m00 * f9 - this.m01 * f7 + this.m02 * f6;
        double f20 = this.m31 * f5 - this.m32 * f4 + this.m33 * f3;
        double f21 = -this.m30 * f5 + this.m32 * f2 - this.m33 * f1;
        double f22 = this.m30 * f4 - this.m31 * f2 + this.m33 * f;
        double f23 = -this.m30 * f3 + this.m31 * f1 - this.m32 * f;
        double f24 = -this.m21 * f5 + this.m22 * f4 - this.m23 * f3;
        double f25 = this.m20 * f5 - this.m22 * f2 + this.m23 * f1;
        double f26 = -this.m20 * f4 + this.m21 * f2 - this.m23 * f;
        double f27 = this.m20 * f3 - this.m21 * f1 + this.m22 * f;
        this.m00 = f12;
        this.m10 = f13;
        this.m20 = f14;
        this.m30 = f15;
        this.m01 = f16;
        this.m11 = f17;
        this.m21 = f18;
        this.m31 = f19;
        this.m02 = f20;
        this.m12 = f21;
        this.m22 = f22;
        this.m32 = f23;
        this.m03 = f24;
        this.m13 = f25;
        this.m23 = f26;
        this.m33 = f27;
        return f * f11 - f1 * f10 + f2 * f9 + f3 * f8 - f4 * f7 + f5 * f6;
    }

    public Mat4 transpose() {
        double f = this.m10;
        this.m10 = this.m01;
        this.m01 = f;
        f = this.m20;
        this.m20 = this.m02;
        this.m02 = f;
        f = this.m21;
        this.m21 = this.m12;
        this.m12 = f;
        f = this.m30;
        this.m30 = this.m03;
        this.m03 = f;
        f = this.m31;
        this.m31 = this.m13;
        this.m13 = f;
        f = this.m32;
        this.m32 = this.m23;
        this.m23 = f;

        return this;
    }

    public boolean invert() {
        double d = this.adjugateAndDet();
        if(Math.abs(d) > 1.0E-6F) {
           this.mul(d);
           return true;
        } else {
           return false;
        }
    }

    private void mul(double d) {
        this.m00 *= d;
        this.m01 *= d;
        this.m02 *= d;
        this.m03 *= d;
        this.m10 *= d;
        this.m11 *= d;
        this.m12 *= d;
        this.m13 *= d;
        this.m20 *= d;
        this.m21 *= d;
        this.m22 *= d;
        this.m23 *= d;
        this.m30 *= d;
        this.m31 *= d;
        this.m32 *= d;
        this.m33 *= d;
    }

    public Mat4 mul(Mat4 m) {
        double m00 = this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20 + this.m03 * m.m30;
        double m01 = this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21 + this.m03 * m.m31;
        double m02 = this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22 + this.m03 * m.m32;
        double m03 = this.m00 * m.m03 + this.m01 * m.m13 + this.m02 * m.m23 + this.m03 * m.m33;
        double m10 = this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20 + this.m13 * m.m30;
        double m11 = this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21 + this.m13 * m.m31;
        double m12 = this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22 + this.m13 * m.m32;
        double m13 = this.m10 * m.m03 + this.m11 * m.m13 + this.m12 * m.m23 + this.m13 * m.m33;
        double m20 = this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20 + this.m23 * m.m30;
        double m21 = this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21 + this.m23 * m.m31;
        double m22 = this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22 + this.m23 * m.m32;
        double m23 = this.m20 * m.m03 + this.m21 * m.m13 + this.m22 * m.m23 + this.m23 * m.m33;
        double m30 = this.m30 * m.m00 + this.m31 * m.m10 + this.m32 * m.m20 + this.m33 * m.m30;
        double m31 = this.m30 * m.m01 + this.m31 * m.m11 + this.m32 * m.m21 + this.m33 * m.m31;
        double m32 = this.m30 * m.m02 + this.m31 * m.m12 + this.m32 * m.m22 + this.m33 * m.m32;
        double m33 = this.m30 * m.m03 + this.m31 * m.m13 + this.m32 * m.m23 + this.m33 * m.m33;
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        return this;
    }

    public Mat4 mul(Quaternion q) {
        return this.mul(new Mat4(q));
    }

    //

    public Mat4 translate(double x, double y, double z) {
        return this.mul(createTranslation(x, y, z));
    }

    public Mat4 translate(Vec3 v) {
        return this.translate(v.x, v.y, v.z);
    }

    public Mat4 scale(double x, double y, double z) {
        return this.mul(createScale(x, y, z));
    }

    public Mat4 scale(Vec3 v) {
        return this.scale(v.x, v.y, v.z);
    }

    public Mat4 rotate(Quaternion q) {
        return this.mul(q);
    }

    public Mat4 rotateRad(Vector3f axis, float angle) {
        return this.mul(axis.rotation((float) (angle % 2 * Math.PI)));
    }

    public Mat4 rotateDeg(Vector3f axis, float angle) {
        return this.mul(new Quaternion(axis, angle % 360, true));
    }

    public static Mat4 createTranslation(double x, double y, double z) {
        Mat4 m = Mat4.identity();
        m.m03 = x;
        m.m13 = y;
        m.m23 = z;
        return m;
    }

    public static Mat4 createScale(double x, double y, double z) {
        Mat4 m = Mat4.identity();
        m.m00 = x;
        m.m11 = y;
        m.m22 = z;
        return m;
    }

    public Matrix4f to4f() {
        return new Matrix4f(new float[] {
                (float)m00, (float)m01, (float)m02, (float)m03,
                (float)m10, (float)m11, (float)m12, (float)m13,
                (float)m20, (float)m21, (float)m22, (float)m23,
                (float)m30, (float)m31, (float)m32, (float)m33
        });
    }

    public FloatBuffer toBuffer() {
        FloatBuffer buffer = FloatBuffer.allocate(16);
        this.store(buffer);
        return buffer;
    }
}