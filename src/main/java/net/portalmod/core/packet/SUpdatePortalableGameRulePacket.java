package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.init.GameRuleInit;

import java.util.function.Supplier;

public class SUpdatePortalableGameRulePacket implements AbstractPacket<SUpdatePortalableGameRulePacket> {
    private boolean value;

    public SUpdatePortalableGameRulePacket() {}

    public SUpdatePortalableGameRulePacket(boolean value) {
        this.value = value;
    }

    @Override
    public SUpdatePortalableGameRulePacket decode(PacketBuffer buffer) {
        boolean value = buffer.readBoolean();
        return new SUpdatePortalableGameRulePacket(value);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(this.value);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWorld level = Minecraft.getInstance().level;
                if(level == null)
                    return;

                level.getGameRules().getRule(GameRuleInit.USE_PORTALABLE_BLACKLIST).set(this.value, null);
            });
        });
        context.get().setPacketHandled(true);
        return true;
    }
}
