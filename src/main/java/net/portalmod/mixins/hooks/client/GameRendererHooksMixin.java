package net.portalmod.mixins.hooks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.portalmod.client.event.CameraSetupEvent;
import net.portalmod.core.event.ClientEvents;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Forge's EntityViewRenderEvent.CameraSetup: lets the mod change the camera yaw, pitch
 * and roll right after vanilla sets the camera up, before the view rotation is applied.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererHooksMixin {
    @Shadow @Final private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"))
    private void pmOnCameraSetup(float partialTicks, long finishNanoTime, PoseStack poseStack, CallbackInfo ci) {
        Camera camera = this.mainCamera;
        CameraSetupEvent event = new CameraSetupEvent(camera, partialTicks, camera.getYRot(), camera.getXRot(), 0.0F);
        ClientEvents.onCameraSetup(event);
        ((ActiveRenderInfoAccessor) camera).pmSetRotation(event.getYaw(), event.getPitch());
        if (event.getRoll() != 0.0F)
            poseStack.mulPose(Axis.ZP.rotationDegrees(event.getRoll()));
    }
}
