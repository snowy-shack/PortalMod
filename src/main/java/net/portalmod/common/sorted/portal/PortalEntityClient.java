package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;

import java.util.List;

public class PortalEntityClient {
//    protected static boolean hasConnection() {
//        return Minecraft.getInstance().getConnection() != null;
//    }

    public static void teleportCamera(final EntityViewRenderEvent.CameraSetup event) {
//        if(true)
//            return;

        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        ActiveRenderInfo camera = event.getInfo();
        World level = player.level;

//        if(minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || camera.isDetached())
//            return;
//
//        Vector3d delta = player.getPosition(0).subtract(player.getPosition(1));
//
////        Vector3d eyePos = player.getEyePosition(1);
//        Vector3d pos = player.getPosition(1);
//        AxisAlignedBB bb = player.getBoundingBox();
//
//        while(true) {
//            AxisAlignedBB travelAABB = bb.expandTowards(delta);
////            AxisAlignedBB travelAABB = new AxisAlignedBB(eyePos, eyePos)
////                    .inflate(1).expandTowards(delta);
//
//            List<PortalEntity> portals = getOpenPortals(level, travelAABB, portal ->
//                    portal.isEntityAlignedToPortal(player)
//                    && camera.getPosition().subtract(portal.getCenter()).dot(new Vector3d(portal.getNormal())) < 0);
//
//            if(portals.isEmpty())
//                break;
//
//            PortalEntity portal = portals.get(0);
//            camera.setPosition(portal.teleportPoint(new Vec3(camera.getPosition())).to3d());
////            eyePos = portal.teleportPoint(new Vec3(eyePos)).to3d();
//            Vector3d newPos = portal.teleportPoint(new Vec3(pos)).to3d();
//            bb = bb.move(newPos.subtract(pos));
//            pos = newPos;
//        }





//        Vector3d delta = player.getPosition(0).subtract(player.getPosition(1));
//        AxisAlignedBB travelAABB = player.getBoundingBox().expandTowards(delta);
//
//        List<PortalEntity> portals = getOpenPortals(level, travelAABB, portal ->
//                portal.isEntityAlignedToPortal(player)
//                        && camera.getPosition().subtract(portal.position()).dot(new Vector3d(portal.getNormal())) < 0);
//
//        if(portals.isEmpty())
//            return;
//
//        PortalEntity portal = portals.get(0);
//        camera.setPosition(portal.teleportPoint(new Vec3(camera.getPosition())).to3d());









//        if(true)
//            return;

        List<PortalEntity> entities = PortalEntity.getOpenPortals(level, player.getBoundingBox(), portal -> true);

//        List<Entity> entities = minecraft.level.getEntities(player, player.getBoundingBox());
        for(Entity entity : entities) {
            if(entity instanceof PortalEntity) {
                if(!((PortalEntity)entity).isOpen() || !((PortalEntity)entity).getOtherPortal().isPresent())
                    continue;
//                {
//                    PortalEntity portal = (PortalEntity)entity;
//                    Vec3 normal = new Vec3(portal.direction.step());
//                    Vec3 portalPos = new Vec3(portal.blockPosition()).add(.5)
//                            .sub(normal.clone().mul(.5))
//                            .add(normal.clone().mul(PortalEntityRenderer.OFFSET));
//
//                    PortalEntity targetPortal = PortalPairManager.CLIENT.get(portal.gunUUID).get(portal.end.other());
//                    Vec3 targetNormal = new Vec3(targetPortal.direction.step());
//                    Vec3 targetPortalPos = new Vec3(targetPortal.blockPosition()).add(.5)
//                            .sub(targetNormal.clone().mul(.5))
//                            .add(targetNormal.clone().mul(PortalEntityRenderer.OFFSET));
//
//                    Vec3 cameraPos = new Vec3(camera.getPosition());
//
//                    MatrixStack bobStack = new MatrixStack();
//                    minecraft.gameRenderer.bobHurt(bobStack, (float)event.getRenderPartialTicks());
//                    if(minecraft.options.bobView)
//                        minecraft.gameRenderer.bobView(bobStack, (float)event.getRenderPartialTicks());
//                    Mat4 bob = new Mat4(bobStack.last().pose());
//
//                    Mat4 view = Mat4.IDENTITY
//                            .mul(Vector3f.XP.rotationDegrees(camera.getXRot()))
//                            .mul(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F))
//                            .translate(cameraPos.clone().negate());
//
//                    Vec3 portalPosBob = portalPos.clone().transform(view).transform(bob);
//                    Vec3 normalBob = portalPos.clone().add(normal.clone().negate()).transform(view).transform(bob).sub(portalPosBob);
//
//                    if(portalPosBob.dot(normalBob) < 0)
//                        ((ActiveRenderInfoAccessor)event.getInfo()).pmSetPosition(cameraPos.add(targetPortalPos.sub(portalPos)).to3d());
//                }

                PortalEntity portal = (PortalEntity)entity;
                Vector3f normal = new Vec3(portal.getDirection().getNormal()).to3f();
                Vector3f normal2 = new Vec3(portal.getDirection().getNormal()).to3f();
                normal.mul(.5f);
                normal2.mul(PortalEntityRenderer.OFFSET);
                Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
                        .subtract(new Vector3d(normal)).add(new Vector3d(normal2));

//                PortalEntity targetPortal = PortalPairCache.CLIENT.get(portal.gunUUID).get(portal.end.other());
                PortalEntity targetPortal = portal.getOtherPortal().get();
                Vector3f targetNormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
                Vector3f targetnormal2 = new Vec3(targetPortal.getDirection().getNormal()).to3f();
                targetNormal.mul(.5f);
                targetnormal2.mul(PortalEntityRenderer.OFFSET);
                Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                        .subtract(new Vector3d(targetNormal)).add(new Vector3d(targetnormal2));

                Vector3d cameraPos = camera.getPosition();

                MatrixStack bob = new MatrixStack();
                minecraft.gameRenderer.bobHurt(bob, (float)event.getRenderPartialTicks());
                if(minecraft.options.bobView)
                    minecraft.gameRenderer.bobView(bob, (float)event.getRenderPartialTicks());
                Matrix4f bob4f = bob.last().pose();

                MatrixStack view = new MatrixStack();
                view.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
                view.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
                view.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                Matrix4f view4f = view.last().pose();

                Vector4f portalPosBobbed4f = new Vector4f(new Vector3f(portalPos));
                portalPosBobbed4f.transform(view4f);
                portalPosBobbed4f.transform(bob4f);
                Vector3d portalPosBobbed = new Vector3d(portalPosBobbed4f.x(), portalPosBobbed4f.y(), portalPosBobbed4f.z());

                Vector3f normal3 = new Vec3(portal.getDirection().getOpposite().getNormal()).to3f();
                normal3.add(new Vector3f(portalPos));
                Vector4f normalBobbed4f = new Vector4f(normal3);
                normalBobbed4f.transform(view4f);
                normalBobbed4f.transform(bob4f);
                Vector3d normalBobbed = new Vector3d(normalBobbed4f.x(), normalBobbed4f.y(), normalBobbed4f.z());
                normalBobbed = normalBobbed.subtract(portalPosBobbed);

                if(portalPosBobbed.dot(normalBobbed) < 0) {
                    ((ActiveRenderInfoAccessor) event.getInfo()).pmSetPosition(cameraPos.add(targetPortalPos.subtract(portalPos)));
//                    event.getInfo().setAnglesInternal(90, 0);
                }
            }
        }
    }
}