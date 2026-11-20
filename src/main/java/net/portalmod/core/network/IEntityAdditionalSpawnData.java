package net.portalmod.core.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * An entity that sends extra data when it spawns on the client
 * (same idea as Forge's interface of the same name). See {@link EntitySpawnPackets}.
 */
public interface IEntityAdditionalSpawnData {
    void writeSpawnData(FriendlyByteBuf buffer);

    void readSpawnData(FriendlyByteBuf buffer);
}
