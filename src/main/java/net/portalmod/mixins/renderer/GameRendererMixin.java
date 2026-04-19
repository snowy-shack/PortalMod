package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalRenderer;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow private float zoom;

    @Shadow private float zoomY;

    @Shadow private float zoomX;

    @Shadow protected abstract double getFov(ActiveRenderInfo p_215311_1_, float p_215311_2_, boolean p_215311_3_);

    @Shadow @Final private Minecraft minecraft;

    @Shadow private float renderDistance;

    // BEWARE: PORTAL RENDERING
    @Inject(
                        method = "getProjectionMatrix",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmGetProjectionMatrix(ActiveRenderInfo camera, float partialTicks, boolean b, CallbackInfoReturnable<Matrix4f> info) {
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.last().pose().setIdentity();
        if(this.zoom != 1.0F) {
            matrixstack.translate(this.zoomX, -this.zoomY, 0.0D);
            matrixstack.scale(this.zoom, this.zoom, 1.0F);
        }

        matrixstack.last().pose().multiply(Matrix4f.perspective(this.getFov(camera, partialTicks, b), (float)this.minecraft.getMainRenderTarget().width / (float)this.minecraft.getMainRenderTarget().height, 0.05F, this.renderDistance * 4.0F));
        info.setReturnValue(matrixstack.last().pose());
    }

    @Redirect(
                        method = "pick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/ProjectileHelper;getEntityHitResult(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;D)Lnet/minecraft/util/math/EntityRayTraceResult;"
            )
    )
    private EntityRayTraceResult pmCustomEntityPick(Entity entity, Vector3d from, Vector3d to, AxisAlignedBB aabb, Predicate<Entity> predicate, double d) {
        EntityRayTraceResult rayResult = ProjectileHelper.getEntityHitResult(entity, from, to, aabb, target -> {
            if(target instanceof PortalEntity && entity instanceof LivingEntity)
                if(WrenchItem.hitWithWrench((LivingEntity)entity) || !((PortalEntity)target).isOpen())
                    return true;
            return !target.isSpectator() && target.isPickable();
        }, d);

        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null)
            return rayResult;

        List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(player.level, new Vec3(from), new Vec3(to), portal -> true);
        Mat4 portalMatrix = ModUtil.getMatrixFromPortalChain(portalChain);
        Pair<Vector3d, Vector3d> ray = ModUtil.teleportRay(portalChain, from, to);
        aabb = AABBUtil.transform(aabb, portalMatrix);

        EntityRayTraceResult teleportedRayResult = ProjectileHelper.getEntityHitResult(entity, ray.getFirst(), ray.getSecond(), aabb, target -> {
            if(target instanceof PortalEntity && entity instanceof LivingEntity)
                if(WrenchItem.hitWithWrench((LivingEntity)entity) || !((PortalEntity)target).isOpen())
                    return true;
            return !target.isSpectator() && target.isPickable();
        }, d);

        if(teleportedRayResult == null) {
            return rayResult;
        } else if(rayResult == null) {
            return teleportedRayResult;
        }

        Vector3d teleportedFrom = new Vec3(from).transform(portalMatrix).to3d();
        double realWorldDistance = rayResult.getLocation().subtract(from).lengthSqr();
        double teleportedDistance = teleportedRayResult.getLocation().subtract(teleportedFrom).lengthSqr();

        return realWorldDistance <= teleportedDistance ? rayResult : teleportedRayResult;
    }

    @Inject(
                        method = "shouldRenderBlockOutline",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> info) {
        if(Minecraft.getInstance().player != null) {
            Item mainHandItem = Minecraft.getInstance().player.getMainHandItem().getItem();
            if(mainHandItem instanceof PortalGun) {
                info.setReturnValue(false);
                return;
            }
        }

        if(!PortalRenderer.getInstance().shouldRenderOutline(null))
            info.setReturnValue(false);
    }
}