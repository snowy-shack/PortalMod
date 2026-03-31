package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.portalmod.PMState;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;

import java.util.List;
import java.util.Optional;

public class PortalEntityClient {
    // todo teleport player in spectator

    public static PortalCamera teleportCamera(EntityViewRenderEvent.CameraSetup event, boolean useBobbing) {
        ActiveRenderInfo camera = event.getInfo();
        float partialTicks = (float)event.getRenderPartialTicks();

        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        World level = minecraft.level;

        if(player == null || level == null)
            return null;

        PortalCamera newCamera = new PortalCamera(camera, (float)event.getRenderPartialTicks());
        newCamera.setPitch(event.getPitch());
        newCamera.setYaw(event.getYaw());
        newCamera.setRoll(event.getRoll());

        AxisAlignedBB playerAABB = player.getBoundingBox()
                .move(new Vec3(player.position()).negate().to3d())
                .move(player.xo, player.yo, player.zo)
                .expandTowards(new Vec3(player.position()).sub(player.xo, player.yo, player.zo).to3d());

        if(PMState.cameraPosOverrideForRenderingSelf != null)
            playerAABB = playerAABB.inflate(1);

        List<PortalEntity> entities = PortalEntity.getOpenPortals(level,
                playerAABB.deflate(0.001), portal -> true);

        Optional<PortalEntity> optionalPortal = entities.stream().reduce((o, n) ->
                new Vec3(player.getBoundingBox().getCenter()).sub(n.position()).magnitudeSqr()
                        < new Vec3(player.getBoundingBox().getCenter()).sub(o.position()).magnitudeSqr() ? n : o);

        if(!optionalPortal.isPresent())
            return newCamera;

        PortalEntity portal = optionalPortal.get();

        if(!portal.isOpen() || !portal.getOtherPortal().isPresent())
            return newCamera;

        float affinity = (float)new Vec3(player.getDeltaMovement()).normalize().dot(portal.getDirection().getNormal());
        if(PMState.cameraPosOverrideForRenderingSelf != null && affinity < 0.5)
            return newCamera;

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
        view.mulPose(Vector3f.ZP.rotationDegrees(newCamera.getRoll()));
        view.mulPose(Vector3f.XP.rotationDegrees(newCamera.getXRot()));
        view.mulPose(Vector3f.YP.rotationDegrees(newCamera.getYRot() + 180.0F));
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

            if(useBobbing) {
                if(PMState.cameraPosOverrideForRenderingSelf != null) {
                    PMState.cameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf
                            .clone()
                            .sub(portalPos.add(new Vec3(portal.getNormal()).mul(0.0005f).to3d()))
                            .transform(changeOfBasisMatrix)
                            .add(targetPortalPos.add(new Vec3(targetPortal.getNormal()).mul(0.0005f).to3d()));
                }
            }

            float xRot = newCamera.getXRot();
            float yRot = newCamera.getYRot();
            float roll = newCamera.getRoll();

            OrthonormalBasis cameraBasis = EulerConverter.toVectors(xRot, yRot, roll).transform(changeOfBasisMatrix);
            EulerConverter.EulerAngles angles = EulerConverter.toEulerAngles(cameraBasis);
            ((ActiveRenderInfoAccessor)newCamera).pmSetRotation(angles.getYaw(), angles.getPitch());
            newCamera.setRoll(angles.getRoll());
        }

        return newCamera;
    }

    private static PortalCamera teleportThirdPersonCamera(EntityViewRenderEvent.CameraSetup event) {
        ActiveRenderInfo camera = event.getInfo();
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        World level = minecraft.level;

        if(player == null || level == null)
            return null;

        PortalCamera newCamera = new PortalCamera(camera, (float)event.getRenderPartialTicks());
        newCamera.setPitch(event.getPitch());
        newCamera.setYaw(event.getYaw());
        newCamera.setRoll(event.getRoll());

        Vec3 from = new Vec3(event.getInfo().getEntity().getEyePosition((float)event.getRenderPartialTicks()));
        Vec3 to = new Vec3(newCamera.getPosition());
        List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(Minecraft.getInstance().level, from, to, portal -> true);

        Mat4 matrix = ModUtil.getMatrixFromPortalChain(portalChain);
        Mat4 rotationMatrix = ModUtil.getRotationMatrixFromPortalChain(portalChain);

        Vec3 eyePos = new Vec3(camera.getEntity().getEyePosition((float)event.getRenderPartialTicks()));
        Vec3 newEyePos = eyePos.clone().transform(matrix);

        Vec3 newCameraPos = new Vec3(camera.getPosition())
                .sub(eyePos)
                .transform(rotationMatrix)
                .add(newEyePos);
        ((ActiveRenderInfoAccessor)newCamera).pmSetPosition(newCameraPos.to3d());

        float xRot = newCamera.getXRot();
        float yRot = newCamera.getYRot();
        float roll = newCamera.getRoll();

        OrthonormalBasis cameraBasis = EulerConverter.toVectors(xRot, yRot, roll).transform(rotationMatrix);
        EulerConverter.EulerAngles angles = EulerConverter.toEulerAngles(cameraBasis);
        ((ActiveRenderInfoAccessor)newCamera).pmSetRotation(angles.getYaw(), angles.getPitch());
        newCamera.setRoll(angles.getRoll());

        return newCamera;
    }

    public static void teleportCameraAndApply(EntityViewRenderEvent.CameraSetup event) {
        ActiveRenderInfo camera = event.getInfo();
        boolean thirdPerson = camera.isDetached();
        PortalCamera newCamera = thirdPerson ? teleportThirdPersonCamera(event) : teleportCamera(event, true);

        if(!thirdPerson) {
            PortalCamera cameraForLight = teleportCamera(event, false);

            if(cameraForLight != null) {
                // todo or just dont teleport the block position at all to prevent flickering

                BlockPos lightPos = new BlockPos(new Vec3(cameraForLight.getPosition()).to3i());
                ((ActiveRenderInfoAccessor) camera).pmGetBlockPosition().set(lightPos);
            }
        } else {
            if(newCamera != null) {
                BlockPos lightPos = new BlockPos(new Vec3(newCamera.getPosition()).to3i());
                ((ActiveRenderInfoAccessor) camera).pmGetBlockPosition().set(lightPos);
            }
        }

        if(newCamera != null) {
            ((ActiveRenderInfoAccessor)camera).pmSetPosition(new Vec3(newCamera.getPosition()).to3d());
            ((ActiveRenderInfoAccessor)camera).pmSetRotation(newCamera.getYRot(), newCamera.getXRot());

            event.setPitch(newCamera.getXRot());
            event.setYaw(newCamera.getYRot());
            event.setRoll(newCamera.getRoll());

            if(((ITeleportLerpable)Minecraft.getInstance().player).hasUsedPortal()) {
                Minecraft.getInstance().getProfiler().push("pm_translucent_sort");
                PortalTransparencyHandler.resortMainTransparency(new Vec3(newCamera.getPosition()));
                Minecraft.getInstance().getProfiler().pop();
            }
        }
    }

    public static boolean isLocalPlayerMoving() {
        GameSettings options = Minecraft.getInstance().options;
        return options.keyLeft.isDown()
            || options.keyRight.isDown()
            || options.keyUp.isDown()
            || options.keyDown.isDown();
    }
}