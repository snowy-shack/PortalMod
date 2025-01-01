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
import net.portalmod.common.sorted.pellet.PelletEntity;
import net.portalmod.common.sorted.sign.ChamberSignEntity;

import java.util.UUID;

public class SSpawnPelletPacket implements IPacket<IClientPlayNetHandler> {
    private int id;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private double dx;
    private double dy;
    private double dz;

    public SSpawnPelletPacket(int id, UUID uuid, double x, double y, double z, double dx, double dy, double dz) {
        this.id = id;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.id = buffer.readInt();
        this.uuid = buffer.readUUID();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.dx = buffer.readDouble();
        this.dy = buffer.readDouble();
        this.dz = buffer.readDouble();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(this.id);
        buffer.writeUUID(this.uuid);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeDouble(this.dx);
        buffer.writeDouble(this.dy);
        buffer.writeDouble(this.dz);
    }

    @Override
    public void handle(IClientPlayNetHandler clientPlayNetHandler) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // todo dont use Minecraft

            Minecraft minecraft = Minecraft.getInstance();
            PacketThreadUtil.ensureRunningOnSameThread(this, clientPlayNetHandler, minecraft);

            ClientWorld level = minecraft.level;
            PelletEntity entity = new PelletEntity(level, this.x, this.y, this.z, this.dx, this.dy, this.dz);
            entity.setId(this.id);
            entity.setUUID(this.uuid);
            entity.setPacketCoordinates(this.x, this.y, this.z);
            entity.moveTo(this.x, this.y, this.z);

            level.putNonPlayerEntity(entity.getId(), entity);
        });
    }
}
