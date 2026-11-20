package net.portalmod.mixins.entity;

import net.portalmod.core.interfaces.ITeleportLerpable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Mixin(RemotePlayer.class)
public abstract class RemoteClientPlayerEntityMixin extends LivingEntity {
    protected RemoteClientPlayerEntityMixin(EntityType<? extends LivingEntity> p_i48577_1_, Level p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Inject(
                        method = "aiStep",
            at = @At("HEAD")
    )
    private void pmLerpPosWithPortal(CallbackInfo info) {
        Deque<Tuple<Vec3, Vec3>> lerpPositions = ((ITeleportLerpable)this).getLerpPositions();
        if(lerpPositions.isEmpty() || !this.level.isClientSide)
            return;

        Tuple<Vec3, Vec3> currentLerpPos = lerpPositions.pop();
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