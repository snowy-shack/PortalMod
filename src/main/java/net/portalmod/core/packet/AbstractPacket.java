package net.portalmod.core.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface AbstractPacket<T extends AbstractPacket<T>> {
    void encode(PacketBuffer buffer);
    T decode(PacketBuffer buffer);
    boolean handle(Supplier<NetworkEvent.Context> context);
}