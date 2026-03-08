package net.portalmod.common.sorted.autoportal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.common.sorted.portal.PortalColors;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalPlacer;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;

import java.util.*;

public class AutoPortalTileEntity extends TileEntity implements ITickableTileEntity, IndicatorActivated {
    public UUID gunUUID;
    public PortalEnd end;
    public Integer primaryColor;
    public Integer secondaryColor;

    public UUID lastOpenedUUID;
    public PortalEnd lastOpenedEnd;

    public AutoPortalTileEntity(TileEntityType<?> type) {
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

    public void closePortal() {
        if(this.level != null) {
            PortalEntity portal = (PortalEntity)((ServerWorld) this.level).getEntity(this.lastOpenedUUID);
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
        if (indicatorInfo.hasIndicators) {
            if (isPowered != indicatorInfo.allIndicatorsActivated) {
                ((AutoPortalBlock)blockState.getBlock()).setPowered(indicatorInfo.allIndicatorsActivated, blockState, this.level, this.getBlockPos());

                if(indicatorInfo.allIndicatorsActivated) {
                    Direction facing = this.getBlockState().getValue(AutoPortalBlock.FACING);
                    Direction direction = this.getBlockState().getValue(AutoPortalBlock.DIRECTION);
                    Tuple<Direction, Direction> directions = ((AutoPortalBlock)blockState.getBlock()).placementDirectionsFromFacingAndDirection(facing, direction);
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
                                    position, facing, up, true, null);

                            if(portal != null) {
                                this.lastOpenedUUID = portal.getUUID();
                            }

                            this.sendUpdate();
                        }
                    }
                }
            }
        }
    }

    private void checkLastOpened() {
        if(this.level == null || this.lastOpenedUUID == null)
            return;

        PortalEntity portal = (PortalEntity)((ServerWorld)this.level).getEntity(this.lastOpenedUUID);
        if(portal == null)
            return;

        if(!portal.isAlive()) {
            this.lastOpenedUUID = null;
            this.sendUpdate();
        }
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, World world, BlockPos pos) {
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
    public CompoundNBT save(CompoundNBT nbt) {
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
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        load(nbt);
    }
    
    public void load(CompoundNBT nbt) {
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
        return new SUpdateTileEntityPacket(this.getBlockPos(), -1, save(new CompoundNBT()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.load(packet.getTag());
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getBlockPos()).inflate(1);
    }
    
    @Override
    public double getViewDistance() {
        return 256.0D;
    }
}