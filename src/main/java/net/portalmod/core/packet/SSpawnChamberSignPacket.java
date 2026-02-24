package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.common.sorted.sign.ChamberSignEntity;

import java.util.UUID;
import java.util.function.Supplier;

public class SSpawnChamberSignPacket implements AbstractPacket<SSpawnChamberSignPacket> {
    private int id;
    private UUID uuid;
    private BlockPos pos;
    private Direction direction;
    private boolean verticallyAligned;

    public SSpawnChamberSignPacket() {}

    public SSpawnChamberSignPacket(int id, UUID uuid, BlockPos pos, Direction direction, boolean verticallyAligned) {
        this.id = id;
        this.uuid = uuid;
        this.pos = pos;
        this.direction = direction;
        this.verticallyAligned = verticallyAligned;
    }

    @Override
    public SSpawnChamberSignPacket decode(PacketBuffer buffer) {
        int id = buffer.readInt();
        UUID uuid = buffer.readUUID();
        BlockPos blockPos = buffer.readBlockPos();
        Direction direction = Direction.from2DDataValue(buffer.readInt());
        boolean verticallyAligned = buffer.readBoolean();
        return new SSpawnChamberSignPacket(id, uuid, blockPos, direction, verticallyAligned);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.id);
        buffer.writeUUID(this.uuid);
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.direction.get2DDataValue());
        buffer.writeBoolean(this.verticallyAligned);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientWorld level = Minecraft.getInstance().level;
                if (level == null) return;

                ChamberSignEntity entity = new ChamberSignEntity(level, this.pos, this.direction, this.verticallyAligned);
                entity.setId(this.id);
                entity.setUUID(this.uuid);

                level.putNonPlayerEntity(entity.getId(), entity);
            });
        });
        context.get().setPacketHandled(true);
        return true;
    }
}
