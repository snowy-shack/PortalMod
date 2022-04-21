package io.github.serialsniper.portalmod.common.blockentities;

import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AntlineTileEntity extends TileEntity {
    private final SideMap sideMap = new SideMap();

    public SideMap getSideMap() {
        return sideMap;
    }

    public AntlineTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public AntlineTileEntity() {
        this(TileEntityTypeInit.ANTLINE.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        if(sideMap.isBlank())
            sideMap.makeDefault();
        return super.save(sideMap.get(nbt));
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        sideMap.set(nbt);
    }

    public void load(CompoundNBT nbt, boolean complete) {
        if(complete)
            sideMap.set(nbt);
        else
            sideMap.merge(nbt);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return sideMap.toModelData();
    }

    // chunk update

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        load(state, tag);
    }

    // block update

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        // todo sync only needed data
        return new SUpdateTileEntityPacket(this.getBlockPos(), -1, save(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        // todo false with only needed data
        this.load(packet.getTag(), true);
    }

    public static class SideMap extends HashMap<Direction, Side> {
        public static final ModelProperty<SideMap> MODEL_PROPERTY = new ModelProperty<>();

        public void makeDefault() {
            for(Direction direction : Direction.values())
                put(direction, direction == Direction.DOWN ? Side.dot(direction) : Side.empty(direction));
        }

        // todo implement get wire list

        public SideMap() {
            for(Direction direction : Direction.values())
                this.put(direction, Side.empty(direction));
        }

        public boolean isBlank() {
            for(Direction direction : Direction.values())
                if(!get(direction).isEmpty())
                    return false;
            return true;
        }

        public boolean hasSide(Direction direction) {
            return !get(direction).isEmpty();
        }

        public void set(CompoundNBT nbt) {
            CompoundNBT data = nbt.getCompound("data");
            for(Direction direction : Direction.values())
                this.put(direction, new Side(direction, data.getByte(direction.getName())));
        }

        public CompoundNBT get(CompoundNBT nbt) {
            CompoundNBT data = new CompoundNBT();
            for(Direction direction : Direction.values())
                data.putByte(direction.getName(), get(direction).value);
            nbt.put("data", data);
            return nbt;
        }

        public void merge(CompoundNBT nbt) {
            for(String s : nbt.getAllKeys())
                put(Direction.valueOf(s.toUpperCase()), new Side(Direction.valueOf(s.toUpperCase()), nbt.getByte(s)));
        }

        public int getSideCount() {
            return (int)values().stream().filter(v -> v.value != 0).count();
        }

        public IModelData toModelData() {
            ModelDataMap.Builder builder = new ModelDataMap.Builder();
//            for(Direction direction : Direction.values())
//                builder.withInitial(PROPERTIES.get(direction), get(direction));
            builder.withInitial(MODEL_PROPERTY, this);
            return builder.build();
        }
    }

    public static class Side {
        private byte value;
        private final Direction side;

        public void setValue(byte value) {
            checkValid(value);
            this.value = value;
        }

        // todo make common constants

        public Side(Direction side, byte value) {
            checkValid(value);
            this.side = side;
            this.value = value;
        }

        public Side(Direction side, int value) {
            this(side, (byte)value);
        }

        public static Side empty(Direction direction) {
            return new Side(direction, 0);
        }

        public static Side dot(Direction direction) {
            return new Side(direction, 0xF);
        }

        public boolean isEmpty() {
            return value == 0;
        }

        public boolean isConnectable() {
//            return getValue() != 0 && !(getValue() % 3 == 0 || getValue() % 5 == 0);
            return getValue() == 0xF || countBits(getValue()) == 1;
        }

        public boolean isConnectableWith(Direction direction) {
            return isConnectable() && !hasConnection(direction);
        }

        public static byte valueByDirection(Direction direction) {
            switch(direction) {
                case NORTH: return 0b1000;
                case EAST:  return 0b0100;
                case SOUTH: return 0b0010;
                case WEST:  return 0b0001;
            }
            return 0;
        }

        private Direction toRelative(Direction direction) {
            Direction returnDirection = direction;

            if(side.getAxis() != Direction.Axis.Y) {
                if(direction == Direction.UP)
                    returnDirection = Direction.NORTH;
                if(direction == Direction.DOWN)
                    returnDirection = Direction.SOUTH;
                if(side == Direction.SOUTH && direction.getAxis() == Direction.Axis.X)
                    returnDirection = direction.getOpposite();
                if(side == Direction.EAST) {
                    if(direction == Direction.NORTH)
                        returnDirection = Direction.WEST;
                    if(direction == Direction.SOUTH)
                        returnDirection = Direction.EAST;
                }
                if(side == Direction.WEST) {
                    if(direction == Direction.NORTH)
                        returnDirection = Direction.EAST;
                    if(direction == Direction.SOUTH)
                        returnDirection = Direction.WEST;
                }
            }
            return returnDirection;
        }

        public boolean hasConnection(Direction direction) {
            return getConnections().get(toRelative(direction));
        }

        public HashMap<Direction, Boolean> getConnections() {
            HashMap<Direction, Boolean> connections = new HashMap<>();

            for(int i = 0; i < 4; i++)
                connections.put(Direction.from2DDataValue((i + 2) % 4), getValue() != 0xF && ((getValue() >> (3 - i)) & 1) != 0);
            return connections;
        }

        public void addConnection(Direction direction) {
            direction = toRelative(direction);

            if(getValue() != 0xF)
                value |= valueByDirection(direction);
            else
                value = (byte)((value & 0xF0) | valueByDirection(direction));
            checkValid(value);
        }

        public void removeConnection(Direction direction) {
            if(countBits(getValue()) == 1) {
                value = (byte)(value | 0xF);
                return;
            }
            if(getValue() != 0xF)
                value = (byte)((value & 0xF0) | (value & 0xF) & ~valueByDirection(direction));
        }

        public List<Direction> getPresentConnections() {
            HashMap<Direction, Boolean> connections = getConnections();
            List<Direction> presentConnections = new ArrayList<>();

            connections.forEach((direction, b) -> {
                if(b) presentConnections.add(direction);
            });
            return presentConnections;
        }

        public enum Center { TRUE, FALSE, CORNER }

        public Center getCenter() {
            if(getValue() == 0)
                return Center.FALSE;
            if(getValue() != 0xF && getValue() % 3 == 0)
                return Center.CORNER;
            return Center.TRUE;
        }

        public byte getValue() {
            return (byte)(value & 0xF);
        }

        public boolean isActive() {
            return (value & 0b10000) != 0;
        }

        private void checkValid(byte value) {
            if(countBits(value) == 3)
                throw new IllegalStateException("Antlines with three connections not allowed");
        }

        private int countBits(byte value) {
            int count = 0;
            for(int i = 0; i < 4; i++)
                count += ((value >> i) & 1) != 0 ? 1 : 0;
            return count;
        }
    }
}