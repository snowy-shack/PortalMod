package net.portalmod.mixins.renderer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ActiveRenderInfo.class)
public abstract class ActiveRenderInfoMixin {

    @Inject(
                        method = "getMaxZoom",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void pmTeleportCameraRay(double zoom, CallbackInfoReturnable<Double> info) {
        ActiveRenderInfo thiss = (ActiveRenderInfo)(Object)this;
        Entity entity = thiss.getEntity();
        Vec3 from = new Vec3(thiss.getPosition());
        Vec3 to = from.clone().sub(new Vec3(thiss.getLookVector()).mul(zoom));

        List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(entity.level, from, to, portal -> true);
        if(portalChain.isEmpty()) {
            List<PortalEntity> portals = PortalEntity.getOpenPortals(entity.level, entity.getBoundingBox(), portal -> true);
            if(portals.isEmpty())
                return;

            Vec3 from2 = from.clone().sub(new Vec3(thiss.getLookVector()).mul(0.2));
            RayTraceContext context = new RayTraceContext(from2.to3d(), to.to3d(), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, thiss.getEntity());
            RayTraceResult result = entity.level.clip(context);
            double maxZoom = Math.min(zoom, result.getLocation().distanceTo(from.to3d()) - 0.1);
            info.setReturnValue(maxZoom);

            return;
        }

        Mat4 portalMatrix = ModUtil.getMatrixFromPortalChain(portalChain);
        Vec3 teleportedEyePos = from.clone().transform(portalMatrix);
        Pair<Vector3d, Vector3d> ray = ModUtil.teleportRay(portalChain, from.to3d(), to.to3d());

        RayTraceContext context = new RayTraceContext(ray.getFirst(), ray.getSecond(), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, thiss.getEntity());
        RayTraceResult result = entity.level.clip(context);
        double maxZoom = Math.min(zoom, result.getLocation().distanceTo(teleportedEyePos.to3d()) - 0.1);
        info.setReturnValue(maxZoom);
    }
}