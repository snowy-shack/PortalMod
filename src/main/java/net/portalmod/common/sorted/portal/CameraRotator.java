package net.portalmod.common.sorted.portal;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

public class CameraRotator {
    public static void rotate(Entity entity, PortalEntity portal, PortalEntity targetPortal) {
        boolean isLocalPlayer = entity instanceof ClientPlayerEntity;

        float xRotOld = entity.xRot;
        float yRotOld = entity.yRot;
        float pitch = entity.xRot;
        float yaw = entity.yRot;

        OrthonormalBasis portalBasis = portal.getSourceBasis();
        OrthonormalBasis targetPortalBasis = targetPortal.getDestinationBasis();
        Mat4 changeOfBasisMatrix = portalBasis.getChangeOfBasisMatrix(targetPortalBasis);

        OrthonormalBasis cameraBasis = EulerConverter.toVectors(pitch, yaw, 0).transform(changeOfBasisMatrix);
        EulerConverter.EulerAngles angles = EulerConverter.toEulerAngles(cameraBasis);
        EulerConverter.EulerAngles anglesUnbound = EulerConverter.toEulerAnglesLeastRoll(cameraBasis);

        float absPitch = Math.abs(anglesUnbound.getPitch());

        if(absPitch >= 90 && absPitch <= 135) {
            pitch = MathHelper.clamp(anglesUnbound.getPitch(), -90, 90);
            yaw = anglesUnbound.getYaw();

            if(targetPortal.getDirection().getAxis().isHorizontal()) {
                Vec3 yawVec = new Vec3(targetPortal.getDirection()).mul(-Math.signum(anglesUnbound.getPitch()));
                float yawSin = (float)Vec3.zAxis().cross(yawVec).y;
                float yawCos = (float)Vec3.zAxis().dot(yawVec);
                yaw = -(float)(Math.atan2(yawSin, yawCos) * 180 / Math.PI);

                if(isLocalPlayer) {
                    CameraAnimator.getInstance().startYawAnimation(anglesUnbound.getYaw(), yaw, 700, true);
                }
            }

            if(isLocalPlayer) {
                CameraAnimator.getInstance().startPitchAnimation(anglesUnbound.getPitch(), pitch, 500, true);
                CameraAnimator.getInstance().startRollAnimation(anglesUnbound.getRoll(), 0, 700, true);
            }
        } else {
            pitch = angles.getPitch();
            yaw = angles.getYaw();

            float startRoll = angles.getRoll();
            float endRoll = 0;

            if(portal.getDirection() == Direction.UP && targetPortal.getDirection() == Direction.UP) {
                endRoll = portal.getEnd() == PortalEnd.PRIMARY ? 360 : 0;
                startRoll = CameraAnimator.normalizeAngle(startRoll);
            }

            if(isLocalPlayer) {
                CameraAnimator.getInstance().startRollAnimation(startRoll, endRoll, 500, false);
            }
        }

        entity.xRot = entity.xRotO = pitch;
        entity.yRot = entity.yRotO = yaw;

        if(isLocalPlayer) {
            ClientPlayerEntity player = (ClientPlayerEntity)entity;
            player.xBobO += player.xRot - xRotOld;
            player.yBobO += player.yRot - yRotOld;
            player.xBob += player.xRot - xRotOld;
            player.yBob += player.yRot - yRotOld;
        }
    }
}