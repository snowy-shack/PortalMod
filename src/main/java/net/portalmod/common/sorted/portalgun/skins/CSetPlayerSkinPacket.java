package net.portalmod.common.sorted.portalgun.skins;

import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public class CSetPlayerSkinPacket implements AbstractPacket<CSetPlayerSkinPacket> {
    private String skin;
    private int tint;

    public CSetPlayerSkinPacket() {}

    public CSetPlayerSkinPacket(String skin, int tint) {
        this.skin = skin;
        this.tint = tint;
    }

    public String getSkin() {
        return this.skin;
    }

    public int getTint() {
        return this.tint;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.skin.length());
        buffer.writeCharSequence(this.skin, StandardCharsets.UTF_8);
        buffer.writeInt(this.tint);
    }

    @Override
    public CSetPlayerSkinPacket decode(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        String skin = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString();
        int tint = buffer.readInt();
        return new CSetPlayerSkinPacket(skin, tint);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if(player != null)
                ServerSkinManager.getInstance().onServerReceivedPacket(player, this);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}