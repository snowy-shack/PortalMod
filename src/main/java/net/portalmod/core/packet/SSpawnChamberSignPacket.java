package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.portalmod.common.sorted.sign.ChamberSignEntity;

import java.util.UUID;

public class SSpawnChamberSignPacket implements IPacket<IClientPlayNetHandler> {
    private int id;
    private UUID uuid;
    private BlockPos pos;
    private Direction direction;

    public SSpawnChamberSignPacket(int id, UUID uuid, BlockPos pos, Direction direction) {
        this.id = id;
        this.uuid = uuid;
        this.pos = pos;
        this.direction = direction;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.id = buffer.readInt();
        this.uuid = buffer.readUUID();
        this.pos = buffer.readBlockPos();
        this.direction = Direction.from2DDataValue(buffer.readInt());
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(this.id);
        buffer.writeUUID(this.uuid);
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.direction.get2DDataValue());
    }

    @Override
    public void handle(IClientPlayNetHandler clientPlayNetHandler) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // todo dont use Minecraft

            Minecraft minecraft = Minecraft.getInstance();
            PacketThreadUtil.ensureRunningOnSameThread(this, clientPlayNetHandler, minecraft);

            ClientWorld level = minecraft.level;
            ChamberSignEntity entity = new ChamberSignEntity(level, this.pos, this.direction);
            entity.setId(this.id);
            entity.setUUID(this.uuid);

            level.putNonPlayerEntity(entity.getId(), entity);
        });
    }
}
