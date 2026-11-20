package net.portalmod.common.sorted.portalgun.skins;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class SSetPlayerSkinPacket implements AbstractPacket<SSetPlayerSkinPacket> {
    private UUID player;
    private String skin;
    private int tint;

    public SSetPlayerSkinPacket() {}

    public SSetPlayerSkinPacket(UUID player, String skin, int tint) {
        this.player = player;
        this.skin = skin;
        this.tint = tint;
    }

    public UUID getPlayer() {
        return this.player;
    }

    public String getSkin() {
        return this.skin;
    }

    public int getTint() {
        return this.tint;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.player);
        buffer.writeInt(this.skin.length());
        buffer.writeCharSequence(this.skin, StandardCharsets.UTF_8);
        buffer.writeInt(this.tint);
    }

    @Override
    public SSetPlayerSkinPacket decode(FriendlyByteBuf buffer) {
        UUID player = buffer.readUUID();
        int strlen = buffer.readInt();
        String skin = buffer.readCharSequence(strlen, StandardCharsets.UTF_8).toString();
        int tint = buffer.readInt();
        return new SSetPlayerSkinPacket(player, skin, tint);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientOnly.run(() -> () -> ClientSkinManager.getInstance().onClientReceivedPacket(this)));

        context.get().setPacketHandled(true);
        return true;
    }
}