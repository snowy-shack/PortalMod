package net.portalmod.common.sorted.trigger;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.PacketInit;

import java.util.HashMap;

public class TriggerSelectionServer {

    // Server map for keeping track of who is configuring which triggers
    public static final HashMap<ServerPlayerEntity, TriggerTileEntity> TRIGGER_PER_PLAYER = new HashMap<>();

    public static void startConfiguration(ServerPlayerEntity player, TriggerTileEntity trigger) {
        trigger.startConfiguration(player);
        TriggerSelectionServer.TRIGGER_PER_PLAYER.put(player, trigger);
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new STriggerStartConfigPacket(trigger.getBlockPos()));
    }

    public static void endConfiguration(ServerPlayerEntity player) {
        if (TriggerSelectionServer.TRIGGER_PER_PLAYER.containsKey(player)) {
            TriggerSelectionServer.TRIGGER_PER_PLAYER.get(player).endConfiguration();
        }
        TriggerSelectionServer.TRIGGER_PER_PLAYER.remove(player);
    }
}