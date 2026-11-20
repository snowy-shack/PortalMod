package net.portalmod.core.packet;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class SSpawnChamberSignPacket implements AbstractPacket<SSpawnChamberSignPacket> {
    public int id;
    public UUID uuid;
    public BlockPos pos;
    public Direction direction;
    public boolean verticallyAligned;

    public SSpawnChamberSignPacket() {}

    public SSpawnChamberSignPacket(int id, UUID uuid, BlockPos pos, Direction direction, boolean verticallyAligned) {
        this.id = id;
        this.uuid = uuid;
        this.pos = pos;
        this.direction = direction;
        this.verticallyAligned = verticallyAligned;
    }

    @Override
    public SSpawnChamberSignPacket decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        UUID uuid = buffer.readUUID();
        BlockPos blockPos = buffer.readBlockPos();
        Direction direction = Direction.from2DDataValue(buffer.readInt());
        boolean verticallyAligned = buffer.readBoolean();
        return new SSpawnChamberSignPacket(id, uuid, blockPos, direction, verticallyAligned);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.id);
        buffer.writeUUID(this.uuid);
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.direction.get2DDataValue());
        buffer.writeBoolean(this.verticallyAligned);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> ClientPacketHandler.handleSSpawnChamberSignPacket(this));
        });
        context.get().setPacketHandled(true);
        return true;
    }
}
