package net.portalmod.common.sorted.trigger;

import net.portalmod.core.network.PacketDistributor;
import net.portalmod.core.init.PacketInit;

import java.util.HashMap;

import net.minecraft.server.level.ServerPlayer;

public class TriggerSelectionServer {

    // Server map for keeping track of who is configuring which triggers
    public static final HashMap<ServerPlayer, TriggerTileEntity> TRIGGER_PER_PLAYER = new HashMap<>();

    public static void startConfiguration(ServerPlayer player, TriggerTileEntity trigger) {
        trigger.startConfiguration(player);
        TriggerSelectionServer.TRIGGER_PER_PLAYER.put(player, trigger);
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new STriggerStartConfigPacket(trigger.getBlockPos()));
    }

    public static void endConfiguration(ServerPlayer player) {
        if (TriggerSelectionServer.TRIGGER_PER_PLAYER.containsKey(player)) {
            TriggerSelectionServer.TRIGGER_PER_PLAYER.get(player).endConfiguration();
        }
        TriggerSelectionServer.TRIGGER_PER_PLAYER.remove(player);
    }
}