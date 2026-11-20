package net.portalmod.core.packet;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.init.GameRuleInit;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules;

public class SUpdateBooleanGameRulePacket implements AbstractPacket<SUpdateBooleanGameRulePacket> {
    private String name;
    private boolean value;

    public SUpdateBooleanGameRulePacket() {}

    public SUpdateBooleanGameRulePacket(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public SUpdateBooleanGameRulePacket decode(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        String rule = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString();
        boolean value = buffer.readBoolean();
        return new SUpdateBooleanGameRulePacket(rule, value);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.name.length());
        buffer.writeCharSequence(this.name, StandardCharsets.UTF_8);
        buffer.writeBoolean(this.value);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> {
                ClientLevel level = Minecraft.getInstance().level;
                if(level == null)
                    return;

                level.getGameRules().getRule(GameRuleInit.<GameRules.BooleanValue>getRule(this.name)).set(this.value, null);
            });
        });
        context.get().setPacketHandled(true);
        return true;
    }
}
