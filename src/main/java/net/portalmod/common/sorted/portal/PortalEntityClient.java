package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;

import java.util.List;

public class PortalEntityClient {
//    protected static boolean hasConnection() {
//        return Minecraft.getInstance().getConnection() != null;
//    }

    // todo teleport player in spectator

    public static ActiveRenderInfo teleportCamera(ActiveRenderInfo camera, float partialTicks, boolean useBobbing) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        World level = player.level;

        ActiveRenderInfo newCamera = new ActiveRenderInfo();
        ((ActiveRenderInfoAccessor)newCamera).pmSetPosition(new Vec3(camera.getPosition()).to3d());
        newCamera.setAnglesInternal(camera.getYRot(), camera.getXRot());

        List<PortalEntity> entities = PortalEntity.getOpenPortals(level, player.getBoundingBox(), portal -> true);

        for(Entity entity : entities) {
            if(entity instanceof PortalEntity) {
                if(!((PortalEntity)entity).isOpen() || !((PortalEntity)entity).getOtherPortal().isPresent())
                    continue;

                PortalEntity portal = (PortalEntity)entity;
                Vector3f normal = new Vec3(portal.getDirection().getNormal()).to3f();
                Vector3f normal2 = new Vec3(portal.getDirection().getNormal()).to3f();
                normal.mul(.5f);
                normal2.mul(0.0005f);
                Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
                        .subtract(new Vector3d(normal)).add(new Vector3d(normal2));

//                PortalEntity targetPortal = PortalPairCache.CLIENT.get(portal.gunUUID).get(portal.end.other());
                PortalEntity targetPortal = portal.getOtherPortal().get();
                Vector3f targetNormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
                Vector3f targetnormal2 = new Vec3(targetPortal.getDirection().getNormal()).to3f();
                targetNormal.mul(.5f);
                targetnormal2.mul(0.0005f);
                Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                        .subtract(new Vector3d(targetNormal)).add(new Vector3d(targetnormal2));

                Vector3d cameraPos = camera.getPosition();

                MatrixStack bob = new MatrixStack();
                minecraft.gameRenderer.bobHurt(bob, partialTicks);
                if(minecraft.options.bobView)
                    minecraft.gameRenderer.bobView(bob, partialTicks);
                Matrix4f bob4f = bob.last().pose();

                MatrixStack view = new MatrixStack();
                view.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
                view.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
                view.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                Matrix4f view4f = view.last().pose();

                Vector4f portalPosBobbed4f = new Vector4f(new Vector3f(portalPos));
                portalPosBobbed4f.transform(view4f);
                if(useBobbing)
                    portalPosBobbed4f.transform(bob4f);
                Vector3d portalPosBobbed = new Vector3d(portalPosBobbed4f.x(), portalPosBobbed4f.y(), portalPosBobbed4f.z());

                Vector3f normal3 = new Vec3(portal.getDirection().getOpposite().getNormal()).to3f();
                normal3.add(new Vector3f(portalPos));
                Vector4f normalBobbed4f = new Vector4f(normal3);
                normalBobbed4f.transform(view4f);
                if(useBobbing)
                    normalBobbed4f.transform(bob4f);
                Vector3d normalBobbed = new Vector3d(normalBobbed4f.x(), normalBobbed4f.y(), normalBobbed4f.z());
                normalBobbed = normalBobbed.subtract(portalPosBobbed);

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
        }

        return newCamera;
    }

    public static void teleportCamera(ActiveRenderInfo camera, float partialTicks) {
        ActiveRenderInfo newCamera = teleportCamera(camera, partialTicks, true);
        ActiveRenderInfo cameraForLight = PortalEntityClient.teleportCamera(camera, partialTicks, false);

        // todo or just dont teleport the block position at all to prevent flickering

        BlockPos lightPos = new BlockPos(new Vec3(cameraForLight.getPosition()).to3i());
        ((ActiveRenderInfoAccessor)camera).pmGetBlockPosition().set(lightPos);

        ((ActiveRenderInfoAccessor)camera).pmSetPosition(new Vec3(newCamera.getPosition()).to3d());
        ((ActiveRenderInfoAccessor)camera).pmSetRotation(newCamera.getYRot(), newCamera.getXRot());
    }
}