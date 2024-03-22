package net.portalmod.core.util;

import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

public class MatrixUtil {
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