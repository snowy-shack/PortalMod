package net.portalmod.mixins.entity;

import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.interfaces.ITeleportLerpable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;

@Mixin(RemoteClientPlayerEntity.class)
public abstract class RemoteClientPlayerEntityMixin extends LivingEntity {
    protected RemoteClientPlayerEntityMixin(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Inject(
            remap = false,
            method = "aiStep",
            at = @At("HEAD")
    )
    private void pmLerpPosWithPortal(CallbackInfo info) {
        Deque<Tuple<Vector3d, Vector3d>> lerpPositions = ((ITeleportLerpable)this).getLerpPositions();
        if(lerpPositions.isEmpty() || !this.level.isClientSide)
            return;

        Tuple<Vector3d, Vector3d> currentLerpPos = lerpPositions.pop();
        this.setPos(currentLerpPos.getB().x, currentLerpPos.getB().y, currentLerpPos.getB().z);
        this.xo = currentLerpPos.getA().x;
        this.yo = currentLerpPos.getA().y;
        this.zo = currentLerpPos.getA().z;
        this.xOld = currentLerpPos.getA().x;
        this.yOld = currentLerpPos.getA().y;
        this.zOld = currentLerpPos.getA().z;
        this.lerpSteps = 0;
    }
}