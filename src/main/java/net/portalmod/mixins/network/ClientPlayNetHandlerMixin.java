package net.portalmod.mixins.network;

import net.portalmod.common.sorted.sign.ChamberSignEntity;
import net.portalmod.core.interfaces.ITeleportLerpable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Deque;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetHandlerMixin {
    @Shadow private ClientLevel level;

    @Inject(
                        method = "handleMoveEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFIZ)V"
            )
    )
    private void pmMoveEntityLerpTo(ClientboundMoveEntityPacket entityPacket, CallbackInfo info) {
        this.pmEndTeleportLerpBeforeLerpTo(entityPacket.getEntity(this.level));
    }

    @Inject(
                        method = "handleMoveEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isControlledByLocalInstance()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void pmFixChamberSignTeleporting(ClientboundMoveEntityPacket p_147259_1_, CallbackInfo ci, Entity entity) {
        if (entity instanceof ChamberSignEntity) {
            ci.cancel();
        }
    }

    @Inject(
                        method = "handleTeleportEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFIZ)V"
            )
    )
    private void pmTeleportEntityLerpTo(ClientboundTeleportEntityPacket entityPacket, CallbackInfo ci) {
        this.pmEndTeleportLerpBeforeLerpTo(this.level.getEntity(entityPacket.getId()));
    }

    private void pmEndTeleportLerpBeforeLerpTo(Entity entity) {
        Deque<Tuple<Vec3, Vec3>> lerpPositions = ((ITeleportLerpable)entity).getLerpPositions();
        if(lerpPositions.isEmpty())
            return;

        Vec3 pos = lerpPositions.peekLast().getB();
        entity.setPosAndOldPos(pos.x, pos.y, pos.z);
        lerpPositions.clear();
    }
}