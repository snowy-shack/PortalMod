package net.portalmod.mixins.entity;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.packet.CPlayerPortalTeleportLerpPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(
            remap = false,
            method = "drop",
            at = @At("HEAD")
    )
    private void pmStopHoldingSoundOnDrop(boolean all, CallbackInfoReturnable<Boolean> info) {
        ClientPlayerEntity thiss = (ClientPlayerEntity)(Object)this;
        ItemStack selected = thiss.inventory.getSelected();

        if(selected.getItem() instanceof PortalGun && selected.hasTag()) {
            CompoundNBT nbt = selected.getTag();
            if(nbt != null && nbt.contains("Holding") && nbt.getBoolean("Holding")) {
                PortalGun.dropCube(thiss, selected);
            }
        }
    }

    @Redirect(
            remap = false,
            method = "sendPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/play/ClientPlayNetHandler;send(Lnet/minecraft/network/IPacket;)V"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;isControlledCamera()Z")
            )
    )
    private void pmSendOldPosition(ClientPlayNetHandler connection, IPacket<?> packet) {
        connection.send(packet);

        boolean hasPos = packet instanceof CPlayerPacket.PositionPacket || packet instanceof CPlayerPacket.PositionRotationPacket;
        if(!hasPos) {
            return;
        }

        ClientPlayerEntity thiss = (ClientPlayerEntity)(Object)this;
        PacketInit.INSTANCE.sendToServer(new CPlayerPortalTeleportLerpPacket(new Vector3d(thiss.xo, thiss.yo, thiss.zo)));
    }
}