package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (complete)
            sideMap.set(nbt);
        else
            sideMap.merge(nbt);
    }

//    @Override
//    public void onLoad() {
//        super.onLoad();
//
//        if (level.isClientSide) return; //|| this.initialized
//
//        BlockPos pos = this.getBlockPos();
//        BlockState state = level.getBlockState(pos);
//        Block block = state.getBlock();
//
//        AntlineBlock.onPlaced(state, level, pos, block, this);
//        this.initialized = true;
//    }
//
    @Override
    protected void invalidateCaps() { // On removal of entity
        if (level.isClientSide) return;

        SideMap sidemap = this.getSideMap();
        int count = sidemap.getSideCount();

        for (int i = 0; i < count - 1; i++) {
            AntlineBlock.dropResources(getBlock().defaultBlockState(), level, this.getBlockPos());
        }

        super.invalidateCaps();
    }

    private static Block getBlock() {
        return BlockInit.ANTLINE.get();
    }

    @Override
    public IModelData getModelData() {
        return sideMap.toModelData();
    }

    @Override
    public void rotate(Rotation rotation) {
        SideMap initial = (SideMap) this.sideMap.clone();

        // Horizontal faces
        for (Direction direction : Direction.values()) {
            Side side = initial.get(direction);
            side.setSideDirection(rotation.rotate(side.toDirection()));
            this.sideMap.put(rotation.rotate(direction), side);
        }

        // Up/Down
        this.sideMap.get(Direction.UP).rotate(ModUtil.getRotationAmount(rotation));
        this.sideMap.get(Direction.DOWN).rotate(ModUtil.getRotationAmount(rotation));
    }

    @Override
    public void mirror(Mirror mirror) {
        if (mirror == Mirror.NONE) return;

        SideMap initial = (SideMap) this.sideMap.clone();

        for (Direction direction : Direction.values()) {
            Side side = initial.get(direction);
            // Turn side the other way
            side.setSideDirection(mirror.mirror(side.toDirection()));

            // Flip connections
            boolean onWall = direction.getAxis() != Direction.Axis.Y;
            side.mirror(onWall || (mirror == Mirror.FRONT_BACK));

            this.sideMap.put(mirror.mirror(direction), side);
        }
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
        // TODO sync only needed data
        return new SUpdateTileEntityPacket(this.getBlockPos(), -1, save(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        // TODO false with only needed data
        this.load(packet.getTag(), true);
    }

    public static class SideMap extends HashMap<Direction, Side> {
        public static final ModelProperty<SideMap> MODEL_PROPERTY = new ModelProperty<>();

        public void makeDefault() {
            for(Direction direction : Direction.values())
                put(direction, direction == Direction.DOWN ? Side.dot(direction) : Side.emptySide(direction));
        }

        public SideMap() {
            for (Direction direction : Direction.values())
                this.put(direction, Side.emptySide(direction));
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
            CompoundNBT data = nbt.getCompound("AntlineSides");
            for (Direction direction : Direction.values())
                this.put(direction, new Side(direction, data.getByte(direction.getName())));
//            }
        }

        public CompoundNBT get(CompoundNBT nbt) {
            CompoundNBT data = new CompoundNBT();
            for(Direction direction : Direction.values())
                data.putByte(direction.getName(), this.get(direction).value);
            nbt.put("AntlineSides", data);
            return nbt;
        }

        public void merge(CompoundNBT nbt) {
            for (String s : nbt.getAllKeys())
                this.put(Direction.valueOf(s.toUpperCase()), new Side(Direction.valueOf(s.toUpperCase()), nbt.getByte(s)));
        }

        public int getSideCount() {
            return (int)values().stream().filter(v -> v.value != 0xF).count();
        }

        public void removeSide(Direction direction) {
            this.put(direction, Side.emptySide(direction));
        }

        public IModelData toModelData() {
            ModelDataMap.Builder builder = new ModelDataMap.Builder();
            builder.withInitial(MODEL_PROPERTY, this);
            return builder.build();
        }
    }

    // Values:
    //  0000 - dot
    //  0100 - relative east connected
    //  1111 - empty
    // 0???? - inactive
    // 1???? - active

    public static class Side {
        private byte value;
        private Direction sideDir;

        public Side(Direction sideDir, byte value) {
            this.sideDir = sideDir;
            this.value   = value;
        }

        public static Side emptySide(Direction direction) {
            return new Side(direction, (byte) 0xF);
        }

        public static Side dot(Direction direction) {
            return new Side(direction, (byte) 0);
        }

        public void setActualValue(byte value) {
            this.value = value;
        }

        public void setValue(byte value) {
            this.value = (byte) ((this.value & 0b00010000) | (value & 0x0F));
        }

        public void setSideDirection(Direction direction) {
            this.sideDir = direction;
        }

        public boolean isEmpty() {
            return getValue() == 0xF;
        }

        public boolean isConnectable() {
            return countConnections() <= 2;
        }

        public boolean isConnectableWith(Direction direction) {
            return isConnectable() || hasConnection(direction);
        }

        public Set<Direction> absoluteConnections() {
            return Arrays.stream(Direction.values()).filter(this::hasConnection).collect(Collectors.toSet());
        }

        public static byte valueByDirection(Direction direction) {
            switch (direction) {
                case NORTH: return 0b1000; // 1000 north
                case EAST:  return 0b0100; // 0100 east
                case SOUTH: return 0b0010; // 0010 south
                case WEST:  return 0b0001; // 0001 west
            }
            return 0;
        }

        private Direction toRelative(Direction direction) {
            if (sideDir.getAxis() != Direction.Axis.Y) { // On a wall
                switch (direction) {
                    case UP:    return Direction.NORTH;
                    case DOWN:  return Direction.SOUTH;

                    default: { // Else
                        switch (sideDir) {
                            case NORTH: return direction;
                            case WEST:  return direction.getClockWise();
                            case SOUTH: return direction.getOpposite();
                            case EAST:  return direction.getCounterClockWise();
                        }
                    }
                }
            }
            return direction;
        }

        public boolean hasConnection(Direction direction) {
            return Integer.bitCount(getValue() & valueByDirection(toRelative(direction))) != 0 && getValue() != 0xF;
        }

        public HashMap<Direction, Boolean> getConnections() {
            HashMap<Direction, Boolean> connections = new HashMap<>();
            for (Direction direction : Direction.values()) {
                if (direction.getAxis() == Direction.Axis.Y) continue;
                connections.put(toRelative(direction), hasConnection(direction));
            }
            return connections;
        }

        public void addConnection(Direction direction) {
            int newValue = this.value | valueByDirection(toRelative(direction));
            this.setValue((byte) newValue);
        }

        public void removeConnection(Direction direction) {
            int newValue = this.value & ~valueByDirection(toRelative(direction));
            this.setValue((byte) newValue);
        }

        public enum SideType { NORMAL, NONE, CORNER }

        public SideType getSideType() {
            if (getValue() == 0xF) return SideType.NONE; // Empty side
            if ((getValue() == 0b0101) || (getValue() == 0b1010)) return SideType.NORMAL; // Straight lines
            return SideType.CORNER; // Intersection side
        }

        public byte getValue() {
            return (byte) (value & 0xF);
        }

        public byte getActualValue() {
            return value;
        }

        public boolean isActive() {
            return (value & 0b10000) != 0;
        }

        public void setActive(boolean active) { // Sets the 5th LSB
            this.setActualValue((byte) (active ? (value | 0x10) : (value & 0x0F)));
        }

        public int countConnections() { // Count how many connections a side has
            if (getValue() == 0xF) return 0;
            return Integer.bitCount(getValue());
        }

        public Direction toDirection() {
            return this.sideDir;
        }

        public void rotate(int times) {
            times = Math.floorMod(times, 4);
            byte value = this.getValue();
            this.setValue((byte) (value >> times | value << 4 - times));
        }

        public void mirror(boolean flipEastWest) {
            if (flipEastWest) {
                int east = this.getValue() & valueByDirection(Direction.EAST);
                int west = this.getValue() & valueByDirection(Direction.WEST);
                this.setValue((byte) (this.getValue() & ~east & ~west | east >> 2 | west << 2));
            } else {
                int north = this.getValue() & valueByDirection(Direction.NORTH);
                int south = this.getValue() & valueByDirection(Direction.SOUTH);
                this.setValue((byte) (this.getValue() & ~north & ~south | north >> 2 | south << 2));
            }
        }
    }
}