package net.portalmod.mixins.renderer;

import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.portalmod.common.sorted.portal.ITeleportable2;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalRenderer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow(remap = false) abstract protected int getBlockLightLevel(Entity entity, BlockPos pos);
    @Shadow(remap = false) abstract protected int getSkyLightLevel(Entity entity, BlockPos pos);
    
    @Inject(
            remap = false,
            method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ClippingHelper;DDD)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void pmShouldRender(Entity entity, ClippingHelper clippingHelper, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
        if(PortalRenderer.shouldNotRenderEntity(entity, camX, camY, camZ))
            info.setReturnValue(false);
    }

    // todo only when rendering item in hand i guess
    // todo also teleport eyes instead of moving

    @Inject(
            remap = false,
            method = "getPackedLightCoords(Lnet/minecraft/entity/Entity;F)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void pmDeceiveLightEngine(Entity entity, float partialTicks, CallbackInfoReturnable<Integer> info) {
        BlockPos pos = new BlockPos(entity.getEyePosition(partialTicks));

        AxisAlignedBB travelAABB = entity.getBoundingBox().expandTowards(ModUtil.getOldPos(entity).subtract(entity.position()));
        List<PortalEntity> portals = PortalEntity.getOpenPortals(entity.level, travelAABB, portal -> true);
//        List<PortalEntity> portals = entity.level.getEntitiesOfClass(PortalEntity.class, entity.getBoundingBox());

//        if(true) {
//            info.setReturnValue(240);
//            return;
//        }

//        PortalEntity portal = (PortalEntity)entity.level.getEntity(((ITeleportable2)entity).getJustUsedPortal());
//        System.out.println(portal);

//        if(portal == null)
//            return;
//
//        Vector3d eyePos = entity.getEyePosition(partialTicks);
//        BlockPos newEyePos = new BlockPos(portal.teleportPoint(new Vec3(eyePos)).to3i());

        for(PortalEntity portal : portals) {
            Vector3d eyePos = entity.getEyePosition(partialTicks);
            Vector3d portalToEye = eyePos.subtract(portal.position());

            if(portalToEye.dot(new Vec3(portal.getDirection().step()).to3d()) < 0) {
//                BlockPos portalPos = portal.blockPosition();
//                Direction.Axis portalAxis = portal.getDirection().getAxis();
//                int blockCoord = portalAxis.choose(pos.getX(), pos.getY(), pos.getZ());
//                int portalCoord = portalAxis.choose(portalPos.getX(), portalPos.getY(), portalPos.getZ());
//                pos = pos.relative(portal.getDirection(), Math.abs(portalCoord - blockCoord));
                pos = new BlockPos(portal.teleportPoint(new Vec3(eyePos)).to3i());
                break;
            }
        }
        
        info.setReturnValue(LightTexture.pack(
                this.getBlockLightLevel(entity, pos),
                this.getSkyLightLevel(entity, pos)
        ));
    }
}