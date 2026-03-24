package net.portalmod.common.sorted.portalgun.skins;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

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
    public void encode(PacketBuffer buffer) {
        buffer.writeUUID(this.player);
        buffer.writeInt(this.skin.length());
        buffer.writeCharSequence(this.skin, StandardCharsets.UTF_8);
        buffer.writeInt(this.tint);
    }

    @Override
    public SSetPlayerSkinPacket decode(PacketBuffer buffer) {
        UUID player = buffer.readUUID();
        int strlen = buffer.readInt();
        String skin = buffer.readCharSequence(strlen, StandardCharsets.UTF_8).toString();
        int tint = buffer.readInt();
        return new SSetPlayerSkinPacket(player, skin, tint);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientSkinManager.getInstance().onClientReceivedPacket(this)));

        context.get().setPacketHandled(true);
        return true;
    }
}