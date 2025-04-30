package net.portalmod.common.sorted.portal;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.core.math.Vec3;

public class EulerConverter {
    static class EulerAngles {
        private final float pitch;
        private final float yaw;
        private final float roll;

        protected EulerAngles(float pitch, float yaw, float roll) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
        }

        public float getPitch() {
            return this.pitch;
        }

        public float getYaw() {
            return this.yaw;
        }

        public float getRoll() {
            return this.roll;
        }
    }

    public static EulerAngles toEulerAngles(OrthonormalBasis basis) {
        Vec3 look = basis.getZ();
        Vec3 up = basis.getY();

        float pitch = (float)(Math.acos(look.dot(Vec3.yAxis())) * 180 / Math.PI - 90);

        Vec3 yawVec = Vec3.yAxis().cross(new Vec3(look).cross(Vec3.yAxis()));
        float yawSin = (float)Vec3.zAxis().cross(yawVec).y;
        float yaw = -(float)(Math.atan2(yawSin, Vec3.zAxis().dot(yawVec)) * 180 / Math.PI);

        Vec3 upNew = new Vec3(up);
        Vec3 forwardsNew = new Vec3(look);
        Vec3 rightOrtho = forwardsNew.clone().cross(Vec3.yAxis()).normalize();
        Vec3 upOrtho = rightOrtho.clone().cross(forwardsNew).normalize();

        Vec3 upSinVec = upNew.clone().cross(upOrtho);
        float rollSin = (float)(upSinVec.magnitude() * Math.signum(upSinVec.dot(forwardsNew)));
        float rollCos = (float)upNew.clone().dot(upOrtho);
        float roll = (float)(-Math.atan2(rollSin, rollCos) * 180 / Math.PI);

        return new EulerAngles(pitch, yaw, roll);
    }

    public static EulerAngles toEulerAngles(Vec3 x, Vec3 y) {
        return toEulerAngles(new OrthonormalBasis(x, y));
    }

    public static OrthonormalBasis toVectors(Vec3 orientation) {
        Quaternion rotation = new Quaternion(0, 0, 0, 1);
        rotation.set(0, 0, 0, 1);
        rotation.mul(Vector3f.YP.rotationDegrees(-(float)orientation.y));
        rotation.mul(Vector3f.XP.rotationDegrees( (float)orientation.x));
        rotation.mul(Vector3f.ZP.rotationDegrees( (float)orientation.z));

        Vector3f left = new Vector3f(1, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);
        left.transform(rotation);
        up.transform(rotation);
        return new OrthonormalBasis(new Vec3(left), new Vec3(up));
    }

    public static OrthonormalBasis toVectors(float pitch, float yaw, float roll) {
        return toVectors(new Vec3(pitch, yaw, roll));
    }
}