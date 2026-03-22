package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.init.GameRuleInit;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SUpdateBooleanGameRulePacket implements AbstractPacket<SUpdateBooleanGameRulePacket> {
    private String name;
    private boolean value;

    public SUpdateBooleanGameRulePacket() {}

    public SUpdateBooleanGameRulePacket(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public SUpdateBooleanGameRulePacket decode(PacketBuffer buffer) {
        int length = buffer.readInt();
        String rule = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString();
        boolean value = buffer.readBoolean();
        return new SUpdateBooleanGameRulePacket(rule, value);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.name.length());
        buffer.writeCharSequence(this.name, StandardCharsets.UTF_8);
        buffer.writeBoolean(this.value);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWorld level = Minecraft.getInstance().level;
                if(level == null)
                    return;

                level.getGameRules().getRule(GameRuleInit.<GameRules.BooleanValue>getRule(this.name)).set(this.value, null);
            });
        });
        context.get().setPacketHandled(true);
        return true;
    }
}
