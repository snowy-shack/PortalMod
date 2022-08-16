package io.github.serialsniper.portalmod.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.serialsniper.portalmod.client.render.entity.PortalEntityRenderer;
import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/matrix/MatrixStack;DDD)V",
            shift = At.Shift.AFTER
    ), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/Texture;restoreLastBlurMipmap()V", remap = false),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderTypeBuffers;bufferSource()Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;")
    ), method = "renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
    private void portalmod_renderPortals(MatrixStack stack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture light, Matrix4f mat, CallbackInfo info) {
        PortalEntityRenderer.renderPortals(camera, partialTicks);
    }
    
    @Shadow EntityRendererManager entityRenderDispatcher;
    
    @Inject(at = @At("TAIL"), method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V")
    private void portalmod_renderDuplicateEntity(Entity entity, double x, double y, double z, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, CallbackInfo info) {
        double d0 = MathHelper.lerp((double)partialTicks, entity.xOld, entity.getX());
        double d1 = MathHelper.lerp((double)partialTicks, entity.yOld, entity.getY());
        double d2 = MathHelper.lerp((double)partialTicks, entity.zOld, entity.getZ());
        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
        
        if(entity instanceof PortalEntity)
            return;
        
        List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(1));
        for(Entity e : entities) {
            if(e instanceof PortalEntity) {
                Vector3d offset = ((PortalEntity) e).getRenderOffset();
                if(offset == Vector3d.ZERO)
                    continue;
                
                matrixStack.pushPose();
                matrixStack.translate(offset.x, offset.y, offset.z);
                this.entityRenderDispatcher.render(entity, d0 - x, d1 - y, d2 - z, f, partialTicks, matrixStack, renderTypeBuffer, this.entityRenderDispatcher.getPackedLightCoords(entity, partialTicks));
                matrixStack.popPose();
            }
        }
    }
}