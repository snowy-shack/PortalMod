package net.portalmod.mixins.renderer;

import net.portalmod.core.util.MatrixUtil;

import com.mojang.datafixers.util.Pair;
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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Matrix4f;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow private float zoom;

    @Shadow private float zoomY;

    @Shadow private float zoomX;

    @Shadow protected abstract double getFov(Camera p_215311_1_, float p_215311_2_, boolean p_215311_3_);

    @Shadow @Final private Minecraft minecraft;

    @Shadow private float renderDistance;

    // BEWARE: PORTAL RENDERING
    @Inject(
                        method = "getProjectionMatrix",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmGetProjectionMatrix(Camera camera, float partialTicks, boolean b, CallbackInfoReturnable<Matrix4f> info) {
        PoseStack matrixstack = new PoseStack();
        matrixstack.last().pose().identity();
        if(this.zoom != 1.0F) {
            matrixstack.translate(this.zoomX, -this.zoomY, 0.0D);
            matrixstack.scale(this.zoom, this.zoom, 1.0F);
        }

        matrixstack.last().pose().mul(MatrixUtil.perspective(this.getFov(camera, partialTicks, b), (float)this.minecraft.getMainRenderTarget().width / (float)this.minecraft.getMainRenderTarget().height, 0.05F, this.renderDistance * 4.0F));
        info.setReturnValue(matrixstack.last().pose());
    }

    @Redirect(
                        method = "pick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"
            )
    )
    private EntityHitResult pmCustomEntityPick(Entity entity, net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to, AABB aabb, Predicate<Entity> predicate, double d) {
        EntityHitResult rayResult = ProjectileUtil.getEntityHitResult(entity, from, to, aabb, target -> {
            if(target instanceof PortalEntity && entity instanceof LivingEntity)
                if(WrenchItem.hitWithWrench((LivingEntity)entity) || !((PortalEntity)target).isOpen())
                    return true;
            return !target.isSpectator() && target.isPickable();
        }, d);

        Player player = Minecraft.getInstance().player;
        if(player == null)
            return rayResult;

        List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(player.level(), new Vec3(from), new Vec3(to), portal -> true);
        Mat4 portalMatrix = ModUtil.getMatrixFromPortalChain(portalChain);
        Pair<net.minecraft.world.phys.Vec3, net.minecraft.world.phys.Vec3> ray = ModUtil.teleportRay(portalChain, from, to);
        aabb = AABBUtil.transform(aabb, portalMatrix);

        EntityHitResult teleportedRayResult = ProjectileUtil.getEntityHitResult(entity, ray.getFirst(), ray.getSecond(), aabb, target -> {
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

        net.minecraft.world.phys.Vec3 teleportedFrom = new Vec3(from).transform(portalMatrix).to3d();
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