package net.portalmod.common.sorted.autoportal;

import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.common.sorted.portal.PortalColors;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalPlacer;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ChangeDetector;
import net.portalmod.core.util.ModUtil;

import java.util.*;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class AutoPortalTileEntity extends BlockEntity implements TickableBlockEntity, IndicatorActivated {
    public UUID gunUUID;
    public PortalEnd end;
    public Integer primaryColor;
    public Integer secondaryColor;

    public UUID lastOpenedUUID;
    public PortalEnd lastOpenedEnd;

    private final ChangeDetector powerChangeDetector = new ChangeDetector();

    public AutoPortalTileEntity(BlockEntityType<?> type) {
        super(type);
    }

    public AutoPortalTileEntity() {
        this(TileEntityTypeInit.AUTOPORTAL.get());
    }

    public void link(UUID gunUUID, PortalEnd end, int primaryColor, int secondaryColor) {
        this.gunUUID = gunUUID;
        this.end = end;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.sendUpdate();
    }

    public void swapEnd() {
        this.end = this.end.other();
        this.sendUpdate();
    }

    public void setPowered(boolean value) {
        powerChangeDetector.set(value);
    }

    public void closePortal() {
        if(this.level != null) {
            PortalEntity portal = (PortalEntity)((ServerLevel) this.level).getEntity(this.lastOpenedUUID);
            if(portal != null) {
                portal.remove();
            }
        }

        this.lastOpenedUUID = null;
        this.sendUpdate();
    }

    public void sendUpdate() {
        if(this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }
    
    @Override
    public void tick() {
        if(this.level == null || this.level.isClientSide)
            return;

        BlockState blockState = this.getBlockState();
        if(!(blockState.getBlock() instanceof AutoPortalBlock))
            return;

        this.checkLastOpened();

        IndicatorInfo indicatorInfo = this.checkIndicators(blockState, this.level, this.getBlockPos());
        boolean isPowered = blockState.getValue(AutoPortalBlock.POWERED);

        // Powered by indicators
        if(indicatorInfo.hasIndicators) {
            if(isPowered != indicatorInfo.allIndicatorsActivated) {
                ((AutoPortalBlock)blockState.getBlock()).setAntlinePowered(indicatorInfo.allIndicatorsActivated, blockState, this.level, this.getBlockPos());

                if(indicatorInfo.allIndicatorsActivated) {
                    openPortal(blockState);
                    return;
                }
            }
        }

        if(powerChangeDetector.isRising()) openPortal(blockState);
//        if(powerChangeDetector.isFalling()) closePortal();

        powerChangeDetector.shift();
    }

    private void openPortal(BlockState blockState) {
        Direction facing = this.getBlockState().getValue(AutoPortalBlock.FACING);
        Direction direction = this.getBlockState().getValue(AutoPortalBlock.DIRECTION);
        Tuple<Direction, Direction> directions = ((AutoPortalBlock) blockState.getBlock()).placementDirectionsFromFacingAndDirection(facing, direction);
        Direction left = directions.getA();
        Direction up = directions.getB();

        if(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            left = left.getOpposite();

        Vec3 position = new Vec3(this.getBlockPos()).add(.5)
                .add(new Vec3(left.getOpposite()).mul(.5))
                .add(new Vec3(up).mul(.5))
                .add(new Vec3(facing.getOpposite()).mul(.5));

        if(this.gunUUID != null && this.end != null && this.primaryColor != null && this.secondaryColor != null) {
            Optional<Integer> colorIndex = this.getCurrentColorIndex();
            if(colorIndex.isPresent()) {
                String color = PortalColors.values()[colorIndex.get()].name();
                PortalEntity portal = PortalPlacer.placePortal(this.level, this.end, color, this.gunUUID,
                        position, facing, up, true, null, null);

                if(portal != null) {
                    this.lastOpenedUUID = portal.getUUID();
                }

                this.sendUpdate();
            }
        }
    }

    private void checkLastOpened() {
        if(this.level == null || this.lastOpenedUUID == null)
            return;

        PortalEntity portal = (PortalEntity)((ServerLevel)this.level).getEntity(this.lastOpenedUUID);

        if(portal == null || !portal.isAlive()) {
            this.lastOpenedUUID = null;
            this.sendUpdate();
        }
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, Level world, BlockPos pos) {
        Direction facing = blockState.getValue(AutoPortalBlock.FACING);
        Direction direction = blockState.getValue(AutoPortalBlock.DIRECTION);

        Tuple<Direction, Direction> directions = ((AutoPortalBlock)blockState.getBlock()).placementDirectionsFromFacingAndDirection(facing, direction);
        Direction left = directions.getA();
        Direction up = directions.getB();

        if(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            left = left.getOpposite();

        Direction right = left.getOpposite();
        Direction down = up.getOpposite();

        List<BlockPos> positions = new ArrayList<>();
        positions.add(pos.relative(down));
        positions.add(pos.relative(left));
        positions.add(pos.relative(up).relative(left));
        positions.add(pos.relative(up).relative(up));
        positions.add(pos.relative(up).relative(right).relative(up));
        positions.add(pos.relative(up).relative(right).relative(right));
        positions.add(pos.relative(right).relative(right));
        positions.add(pos.relative(right).relative(down));
        return positions;
    }

    public Optional<Integer> getCurrentColorIndex() {
        return Optional.ofNullable(this.end == PortalEnd.PRIMARY ? this.primaryColor : this.secondaryColor);
    }
    
    @Override
    public CompoundTag save(CompoundTag nbt) {
        if(this.gunUUID != null && this.end != null && this.primaryColor != null && this.secondaryColor != null) {
            nbt.putUUID("gunUUID", this.gunUUID);
            nbt.putString("end", this.end.toString().toLowerCase());
            nbt.putString("primaryColor", PortalColors.values()[this.primaryColor].toString().toLowerCase());
            nbt.putString("secondaryColor", PortalColors.values()[this.secondaryColor].toString().toLowerCase());

            if(this.lastOpenedUUID != null)
                nbt.putUUID("lastOpenedPortal", this.lastOpenedUUID);
        }
        return super.save(nbt);
    }
    
    @Override
    public void load(BlockState state, CompoundTag nbt) {
        super.load(state, nbt);
        load(nbt);
    }
    
    public void load(CompoundTag nbt) {
        if(!nbt.contains("gunUUID") || !nbt.contains("end"))
            return;
        if(!nbt.contains("primaryColor") || !nbt.contains("secondaryColor"))
            return;

        this.gunUUID = nbt.getUUID("gunUUID");
        this.end = PortalEnd.valueOf(nbt.getString("end").toUpperCase());
        this.primaryColor = PortalColors.getIndex(nbt.getString("primaryColor"));
        this.secondaryColor = PortalColors.getIndex(nbt.getString("secondaryColor"));
        this.lastOpenedUUID = null;

        if(nbt.contains("lastOpenedPortal"))
            this.lastOpenedUUID = nbt.getUUID("lastOpenedPortal");
    }

    // chunk update
    
    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }
    
    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        load(state, tag);
    }
    
    // block update

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.getBlockPos(), -1, save(new CompoundTag()));
    }
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.load(packet.getTag());
    }
    
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(1);
    }
    
    @Override
    public double getViewDistance() {
        return 256.0D;
    }
}