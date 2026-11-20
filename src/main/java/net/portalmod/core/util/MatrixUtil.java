package net.portalmod.core.util;

import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;


public class MatrixUtil {
    /** GUI projection planes. 1.16 Forge used near 1000 / far getGuiFarPlane() (3000); vanilla 1.20.1 uses 1000 / 21000. */
    public static final float GUI_NEAR_PLANE = 1000.0F;
    public static final float GUI_FAR_PLANE = 21000.0F;
    /** Z offset vanilla 1.20.1 applies to the GUI pose (was 1000 - getGuiFarPlane() on 1.16). */
    public static final float GUI_Z_OFFSET = -11000.0F;

    /** Same as 1.16 MatrixUtil.orthographic(width, height, near, far): x 0..width, y 0..height (y up). */
    public static org.joml.Matrix4f orthographic(float width, float height, float near, float far) {
        return new org.joml.Matrix4f().setOrtho(0.0F, width, 0.0F, height, near, far);
    }

    /** Same as 1.16 MatrixUtil.perspective(fovDegrees, aspect, near, far). */
    public static org.joml.Matrix4f perspective(double fovDegrees, float aspect, float near, float far) {
        return new org.joml.Matrix4f().setPerspective((float) Math.toRadians(fovDegrees), aspect, near, far);
    }

    public static Mat4 absToRel(Vec3 x, Vec3 y) {
        Vec3 z = x.clone().cross(y);

        return new Mat4(
                x.x, y.x, z.x, 0,
                x.y, y.y, z.y, 0,
                x.z, y.z, z.z, 0,
                0,   0,   0,   1
        );
    }

    public static Mat4 relToAbs(Vec3 x, Vec3 y) {
        Vec3 z = x.clone().cross(y);

        return new Mat4(
                x.x, x.y, x.z, 0,
                y.x, y.y, y.z, 0,
                z.x, z.y, z.z, 0,
                0,   0,   0,   1
        );
    }
}