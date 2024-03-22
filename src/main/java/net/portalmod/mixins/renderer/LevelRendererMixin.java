package net.portalmod.mixins.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.portalmod.common.sorted.portal.PortalRenderer;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Inject(remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/matrix/MatrixStack;DDD)V",
            shift = At.Shift.AFTER,
            ordinal = 2
//    ), slice = @Slice(
//            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/Texture;restoreLastBlurMipmap()V"),
//            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderTypeBuffers;bufferSource()Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;")
    ), method = "renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
    private void pmRenderPortals(MatrixStack stack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture light, Matrix4f mat, CallbackInfo info) {
        PortalRenderer.renderPortals(camera, partialTicks);
    }
    
//    @Final @Shadow(remap = false) public EntityRendererManager entityRenderDispatcher;
    
//    @Inject(remap = false, at = @At("TAIL"), method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V")
//    private void pmRenderDuplicateEntity(Entity entity, double x, double y, double z, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, CallbackInfo info) {
//        PortalRenderer.renderDuplicateEntity(entity, x, y, z, partialTicks, matrixStack, renderTypeBuffer, ((WorldRenderer)(Object)this).entityRenderDispatcher);
//    }

//    @Inject(remap = false, method = "renderEntity", at = @At("HEAD"), cancellable = true)
//    private void pmGetPositionWithDiscontinuousInterpolation(Entity entity, double camX, double camY, double camZ, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, CallbackInfo info) {
//        WorldRenderer thiss = (WorldRenderer)(Object)this;
//
//        if(entity instanceof PortalEntity)
//            return;
//
////        if(true)
////            return;
//
////        if(entity instanceof Cube) {
////            System.out.println(entity.getX() + " " + entity.getY() + " " + entity.getZ()
////                    + " " + entity.xo + " " + entity.yo + " " + entity.zo
////                    + " " + entity.xOld + " " + entity.yOld + " " + entity.zOld
////                    + " " + entity.xOld + " " + entity.yOld + " " + entity.zOld);
////        }
//
////        partialTicks = 1;
////        double d0 = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
////        double d1 = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
////        double d2 = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
////        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
////        thiss.entityRenderDispatcher.render(entity, d0 - camX, d1 - camY, d2 - camZ, f, partialTicks, matrixStack, renderTypeBuffer, thiss.entityRenderDispatcher.getPackedLightCoords(entity, partialTicks));
//
////        info.cancel();
////        if(true)
////            return;
////
//////        Entity thiss = (Entity)(Object)this;
//        World level = entity.level;
//
//        if(!((ITeleportable)entity).hasLastUsedPortal())
//            return;
//
//        PortalEntity portal = (PortalEntity)level.getEntity(((ITeleportable)entity).getLastUsedPortal());
//
//        PortalPair pair = PortalPairManager.CLIENT.get(portal.getGunUUID());
//        if(pair == null)
//            return;
//
//        PortalEntity targetPortal = pair.get(portal.getEnd().other());
//        if(targetPortal == null)
//            return;
//
//        Vector3f normal = portal.getDirection().step();
//        Vector3f normal2 = portal.getDirection().step();
//        normal.mul(.5f);
//        normal2.mul(PortalRenderer.OFFSET * 10);
//        Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
//                .subtract(new Vector3d(normal)).add(new Vector3d(normal2));
//
//        Vector3f targetnormal = targetPortal.getDirection().step();
//        Vector3f targetnormal2 = targetPortal.getDirection().step();
//        targetnormal.mul(.5f);
//        targetnormal2.mul(PortalRenderer.OFFSET * 10);
//        Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
//                .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));
//
//        Vector3d offset = targetPortalPos.subtract(portalPos);
//
//        Vector3d posBeforeOld = new Vector3d(entity.xOld, entity.yOld, entity.zOld);
//        Vector3d posAfterOld =  posBeforeOld.add(offset);
//        Vector3d posAfterNew =  entity.position();
//        Vector3d posBeforeNew = posAfterNew.subtract(offset);
//
//        Vector3d portalToEntityBeforeNew = posBeforeOld.add(entity.getDeltaMovement()).subtract(portalPos);
//        if(portalToEntityBeforeNew.dot(new Vector3d(portal.getDirection().step())) >= 0)
//            return;
//
//        AxisAlignedBB bb = entity.getBoundingBox()
//                .move(Vector3d.ZERO.subtract(entity.position()))
//                .move(entity.xOld, entity.yOld, entity.zOld);
//
////        Vector3d delta = posBeforeNew.subtract(posBeforeOld);
//        Vector3d delta = entity.getDeltaMovement();
//        AxisAlignedBB travelAABB = bb.expandTowards(delta);
//        Vec3 projected = portal.projectPointOnPortalSurface(new Vec3(bb.getCenter().add(delta)));
//
//        boolean isInside = projected.x > -.5
//                        && projected.x < .5
//                        && projected.y > -1
//                        && projected.y < 1;
//
//        if(!portal.getBoundingBox().intersects(travelAABB) || !isInside)
//            return;
//
////        List<PortalEntity> portalsInEntity = level.getEntitiesOfClass(PortalEntity.class, travelAABB, portal -> {
////            if(!portal.isOpen())
////                return false;
////
////            PortalPair pair = PortalPairManager.CLIENT.get(portal.getGunUUID());
////            if(pair == null)
////                return false;
////
////            PortalEntity targetPortal = pair.get(portal.getEnd().other());
////            if(targetPortal == null)
////                return false;
////
////            Vec3 projected = portal.projectPointOnPortalSurface(new Vec3(bb.getCenter().add(entity.getDeltaMovement())));
////
////            return projected.x > -.5
////                    && projected.x < .5
////                    && projected.y > -1
////                    && projected.y < 1;
////        });
////
////        if(portalsInEntity.isEmpty())
////            return;
////
////        PortalEntity portal = portalsInEntity.get(0);
////
////        PortalPair pair = PortalPairManager.CLIENT.get(portal.getGunUUID());
////        if(pair == null)
////            return;
////
////        PortalEntity targetPortal = pair.get(portal.getEnd().other());
////        if(targetPortal == null)
////            return;
//
////        Vector3d entityPos = bb.getCenter().add(entity.getDeltaMovement());
////        Vector3d distance = entityPos.subtract(portalPos);
//
//        double renderX = MathHelper.lerp(partialTicks, posBeforeOld.x, posBeforeNew.x);
//        double renderY = MathHelper.lerp(partialTicks, posBeforeOld.y, posBeforeNew.y);
//        double renderZ = MathHelper.lerp(partialTicks, posBeforeOld.z, posBeforeNew.z);
//
//        Vector3d renderPos = new Vector3d(renderX, renderY, renderZ);
//        Vector3d portalToEntity = renderPos.subtract(portalPos);
//
//        if(portalToEntity.dot(new Vector3d(portal.getDirection().step())) < 0) {
//            renderPos = renderPos.add(offset);
//        }
//
//        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
//        thiss.entityRenderDispatcher.render(entity,
//                renderPos.x - camX, renderPos.y - camY, renderPos.z - camZ,
//                f, partialTicks, matrixStack, renderTypeBuffer,
//                thiss.entityRenderDispatcher.getPackedLightCoords(entity, partialTicks));
//        info.cancel();
//
////        Vector3d pos = entity.position();
////        Vector3d delta = entity.getDeltaMovement();
//
////        Vector3d move = targetPortalPos.subtract(portalPos);
//
////        if(distance.dot(new Vector3d(portal.getDirection().step())) >= 0) {
////            double x = MathHelper.lerp(partialTicks, entity.xo, entity.xo + delta.x);
////            double y = MathHelper.lerp(partialTicks, entity.yo, entity.yo + delta.y);
////            double z = MathHelper.lerp(partialTicks, entity.zo, entity.zo + delta.z);
////            float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
////            thiss.entityRenderDispatcher.render(entity, x - camX, y - camY, z - camZ, f, partialTicks, matrixStack, renderTypeBuffer, thiss.entityRenderDispatcher.getPackedLightCoords(entity, partialTicks));
//////            info.setReturnValue(new Vector3d(x, y, z));
////        } else {
////            double x = MathHelper.lerp(partialTicks, pos.x - delta.x, pos.x);
////            double y = MathHelper.lerp(partialTicks, pos.y - delta.y, pos.y);
////            double z = MathHelper.lerp(partialTicks, pos.z - delta.z, pos.z);
////            float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
////            thiss.entityRenderDispatcher.render(entity, x - camX, y - camY, z - camZ, f, partialTicks, matrixStack, renderTypeBuffer, thiss.entityRenderDispatcher.getPackedLightCoords(entity, partialTicks));
////            info.cancel();
////            info.setReturnValue(new Vector3d(x, y, z));
////        }
//    }

//    @Inject(remap = false, at = @At(value = "INVOKE", target = "checkPoseStack(Lcom/mojang/blaze3d/matrix/MatrixStack;)V", ordinal = 0),
//            method = "renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V",
//            locals = LocalCapture.PRINT)
//    private void pmRenderDuplicateLocalPlayer(CallbackInfo info, MatrixStack p_228426_1_, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, ActiveRenderInfo p_228426_6_, GameRenderer p_228426_7_, LightTexture p_228426_8_, Matrix4f p_228426_9_, CallbackInfo ci, IProfiler iprofiler, Vector3d vector3d, double d0, double d1, double d2, Matrix4f matrix4f, boolean flag, ClippingHelper clippinghelper, float f, boolean flag1, int i, int j, long k, long l, long i1, long j1, long k1, long l1, boolean flag2, IRenderTypeBuffer.Impl irendertypebuffer$impl) {
//        Minecraft mc = Minecraft.getInstance();
//        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
//        
//        if(!camera.isDetached() && mc.cameraEntity == mc.player) {
//            PortalEntityRenderer.renderDuplicateEntity(
//                    mc.player,
//                    d0, d1, d2, p_228426_2_, p_228426_1_,
//                    irendertypebuffer$impl,
//                    ((WorldRenderer)(Object)this).entityRenderDispatcher);
//        }
//    }
}