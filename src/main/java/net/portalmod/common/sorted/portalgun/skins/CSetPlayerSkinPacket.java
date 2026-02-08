package net.portalmod.common.sorted.portalgun.skins;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

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
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.skin.length());
        buffer.writeCharSequence(this.skin, StandardCharsets.UTF_8);
        buffer.writeInt(this.tint);
    }

    @Override
    public CSetPlayerSkinPacket decode(PacketBuffer buffer) {
        int length = buffer.readInt();
        String skin = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString();
        int tint = buffer.readInt();
        return new CSetPlayerSkinPacket(skin, tint);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player != null)
                SkinManager.getServerInstance().onServerReceivedPacket(player, this);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}