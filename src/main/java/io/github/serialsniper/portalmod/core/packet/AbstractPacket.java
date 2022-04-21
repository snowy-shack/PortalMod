package io.github.serialsniper.portalmod.core.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface AbstractPacket {
    void encode(PacketBuffer buffer);
    boolean handle(Supplier<NetworkEvent.Context> context);
}