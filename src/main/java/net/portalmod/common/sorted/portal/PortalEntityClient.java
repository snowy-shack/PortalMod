package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;

import java.util.List;

public class PortalEntityClient {
    // todo teleport player in spectator

    public static ActiveRenderInfo teleportCamera(ActiveRenderInfo camera, float partialTicks, boolean useBobbing) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        if(player == null)
            return camera;

        World level = player.level;
        if(level == null)
            return camera;

        ActiveRenderInfo newCamera = new ActiveRenderInfo();
        ((ActiveRenderInfoAccessor)newCamera).pmSetPosition(new Vec3(camera.getPosition()).to3d());
        newCamera.setAnglesInternal(camera.getYRot(), camera.getXRot());

        List<PortalEntity> entities = PortalEntity.getOpenPortals(level,
                player.getBoundingBox().deflate(0.001), portal -> true);

        for(PortalEntity portal : entities) {
            if(!portal.isOpen() || !portal.getOtherPortal().isPresent())
                continue;

            PortalEntity targetPortal = portal.getOtherPortal().get();
            Vector3d portalPos = portal.position();
            Vector3d targetPortalPos = targetPortal.position();
            Vector3d cameraPos = camera.getPosition();

            MatrixStack bob = new MatrixStack();
            minecraft.gameRenderer.bobHurt(bob, partialTicks);
            if(minecraft.options.bobView)
                minecraft.gameRenderer.bobView(bob, partialTicks);
            Mat4 bobMat = new Mat4(bob.last().pose());

            MatrixStack view = new MatrixStack();
            view.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
            view.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
            view.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            Mat4 viewMat = new Mat4(view.last().pose());

            Vec3 portalPosBobbed = new Vec3(portalPos).transform(viewMat);
            if(useBobbing)
                portalPosBobbed.transform(bobMat);

            Vec3 normalBobbed = new Vec3(portal.getDirection().getOpposite()).add(portalPos).transform(viewMat);
            if(useBobbing)
                normalBobbed.transform(bobMat);
            normalBobbed.sub(portalPosBobbed);

            OrthonormalBasis portalBasis = portal.getSourceBasis();
            OrthonormalBasis targetPortalBasis = targetPortal.getDestinationBasis();
            Mat4 changeOfBasisMatrix = portalBasis.getChangeOfBasisMatrix(targetPortalBasis);

            if(portalPosBobbed.dot(normalBobbed) < 0) {
                Vec3 newCameraPos = new Vec3(cameraPos)
                        .sub(portalPos.add(new Vec3(portal.getNormal()).mul(0.0005f).to3d()))
                        .transform(changeOfBasisMatrix)
                        .add(targetPortalPos.add(new Vec3(targetPortal.getNormal()).mul(0.0005f).to3d()));
                ((ActiveRenderInfoAccessor)newCamera).pmSetPosition(newCameraPos.to3d());

                // todo other axes
                if(portal.getDirection().getAxis().isHorizontal() && targetPortal.getDirection().getAxis().isHorizontal()) {
                    Vec3 forward = Vec3.zAxis().transform(new Mat4(Vector3f.YN.rotationDegrees(camera.getYRot())));
                    forward.transform(changeOfBasisMatrix);
                    float newYaw = (float)(-Math.signum(Vec3.zAxis().cross(forward).dot(Vec3.yAxis())) * Math.acos(forward.dot(Vec3.zAxis())) / Math.PI * 180);
                    ((ActiveRenderInfoAccessor)newCamera).pmSetRotation(newYaw, camera.getXRot());
                }
            }
        }

        return newCamera;
    }

    public static void teleportCamera(ActiveRenderInfo camera, float partialTicks) {
        ActiveRenderInfo newCamera = teleportCamera(camera, partialTicks, true);
        ActiveRenderInfo cameraForLight = teleportCamera(camera, partialTicks, false);

        // todo or just dont teleport the block position at all to prevent flickering

        BlockPos lightPos = new BlockPos(new Vec3(cameraForLight.getPosition()).to3i());
        ((ActiveRenderInfoAccessor)camera).pmGetBlockPosition().set(lightPos);

        ((ActiveRenderInfoAccessor)camera).pmSetPosition(new Vec3(newCamera.getPosition()).to3d());
        ((ActiveRenderInfoAccessor)camera).pmSetRotation(newCamera.getYRot(), newCamera.getXRot());
    }
}