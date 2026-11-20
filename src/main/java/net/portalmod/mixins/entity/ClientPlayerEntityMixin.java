package net.portalmod.mixins.entity;

import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.packet.CPlayerPortalTeleportLerpPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Inject(
                        method = "drop",
            at = @At("HEAD")
    )
    private void pmStopHoldingSoundOnDrop(boolean all, CallbackInfoReturnable<Boolean> info) {
        LocalPlayer thiss = (LocalPlayer)(Object)this;
        ItemStack selected = thiss.getInventory().getSelected();

        if(selected.getItem() instanceof PortalGun && selected.hasTag()) {
            CompoundTag nbt = selected.getTag();
            if(nbt != null && nbt.contains("Holding") && nbt.getBoolean("Holding")) {
                PortalGun.dropCube(thiss, selected);
            }
        }
    }

    @Redirect(
                        method = "sendPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z")
            )
    )
    private void pmSendOldPosition(ClientPacketListener connection, Packet<?> packet) {
        connection.send(packet);

        boolean hasPos = packet instanceof ServerboundMovePlayerPacket.Pos || packet instanceof ServerboundMovePlayerPacket.PosRot;
        if(!hasPos) {
            return;
        }

        LocalPlayer thiss = (LocalPlayer)(Object)this;
        PacketInit.INSTANCE.sendToServer(new CPlayerPortalTeleportLerpPacket(new Vec3(thiss.xo, thiss.yo, thiss.zo)));
    }
}