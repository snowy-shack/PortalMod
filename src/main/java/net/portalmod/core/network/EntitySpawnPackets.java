package net.portalmod.core.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.portalmod.PortalMod;

/**
 * Spawn packet = the vanilla "add entity" packet followed by the entity's extra spawn data.
 * Replaces Forge's NetworkHooks.getEntitySpawningPacket.
 */
public final class EntitySpawnPackets {
    public static final ResourceLocation ID = new ResourceLocation(PortalMod.MODID, "spawn_entity_with_data");

    private EntitySpawnPackets() {}

    public static <E extends Entity & IEntityAdditionalSpawnData> Packet<ClientGamePacketListener> create(E entity) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        new ClientboundAddEntityPacket(entity).write(buf);
        entity.writeSpawnData(buf);
        return ServerPlayNetworking.createS2CPacket(ID, buf);
    }
}
