package net.portalmod.core.packet;

import net.portalmod.core.network.PacketContext;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public interface AbstractPacket<T extends AbstractPacket<T>> {
    void encode(FriendlyByteBuf buffer);
    T decode(FriendlyByteBuf buffer);
    boolean handle(Supplier<PacketContext> context);
}