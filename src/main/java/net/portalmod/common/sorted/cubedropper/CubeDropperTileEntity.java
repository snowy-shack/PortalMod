package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CubeDropperTileEntity extends TileEntity implements ITickableTileEntity {

    public List<UUID> cubeUUIDs = new ArrayList<>();
    public int openTicks = 0;
    public boolean wasPowered = false;

    public CubeDropperTileEntity() {
        super(TileEntityTypeInit.CUBE_DROPPER.get());
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();
        if (!(blockState.getBlock() instanceof CubeDropperBlock)) {
            return;
        }

        if (this.openTicks > 0) {
            this.openTicks++;
        }

        this.updateCubes();

        CubeDropperBlock dropperBlock = (CubeDropperBlock) blockState.getBlock();

        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(this.getBlockPos()));
        possibleIndicatorPositions.addAll(getSurroundingPositions(this.getBlockPos().below()));

        boolean hasIndicators = false;
        boolean allIndicatorsActivated = true;

        for (BlockPos blockPos : possibleIndicatorPositions) {
            BlockState worldBlockState = this.level.getBlockState(blockPos);
            if (worldBlockState.getBlock() instanceof AntlineIndicatorBlock) {
                hasIndicators = true;
                if (!worldBlockState.getValue(AntlineIndicatorBlock.ACTIVE)) {
                    allIndicatorsActivated = false;
                }
            }
        }

        boolean isPowered = blockState.getValue(CubeDropperBlock.POWERED) || hasIndicators && allIndicatorsActivated;

        if (isPowered && (this.openTicks == 0 && this.cubeUUIDs.size() == 1 || !this.wasPowered)) {
            openDropper(dropperBlock);
        }

        this.wasPowered = isPowered;

        if (this.openTicks > 20) {
            closeDropper(dropperBlock);
        }
    }

    // Open dropper and fizzle first cube
    public void openDropper(CubeDropperBlock dropperBlock) {
        dropperBlock.setOpen(true, this.getBlockState(), this.level, this.getBlockPos());

        if (this.level instanceof ServerWorld) {
            this.openTicks = 1;
            if (this.cubeUUIDs.size() == 2) {
                Cube cube = (Cube) ((ServerWorld) this.level).getEntity(this.cubeUUIDs.get(0));
                cube.startFizzling();
                this.cubeUUIDs.remove(0);
            }
        }
    }

    // Close dropper and spawn a new cube inside the dropper
    public void closeDropper(CubeDropperBlock dropperBlock) {
        dropperBlock.setOpen(false, this.getBlockState(), this.level, this.getBlockPos());

        if (this.level instanceof ServerWorld) {
            this.openTicks = 0;
            addCube();
        }
    }

    public void updateCubes() {
        if (this.cubeUUIDs.isEmpty()) {
            addCube();
        }
        this.cubeUUIDs.removeIf(uuid -> this.level instanceof ServerWorld && ((ServerWorld) this.level).getEntity(uuid) == null);
    }

    public void addCube() {
        Cube cube = new Cube(EntityInit.STORAGE_CUBE.get(), this.level);
        Vector3f position = new Vec3(this.getBlockPos().south().east()).to3f();
        cube.teleportTo(position.x(), position.y(), position.z());
        this.cubeUUIDs.add(cube.getUUID());
        this.level.addFreshEntity(cube);
    }

    public void removeAllCubes() {
        if (this.level instanceof ServerWorld) {
            for (UUID uuid : this.cubeUUIDs) {
                Cube cube = (Cube) ((ServerWorld) this.level).getEntity(uuid);
                if (cube != null) {
                    cube.startFizzling();
                }
            }
            this.cubeUUIDs.clear();
        }
    }

    /*
    horizontal >
    vertical ^

    5      6       7      8
    4   dropper  dropper  9
    3   updated  dropper  10
    2      1       12     11

     */
    public static List<BlockPos> getSurroundingPositions(BlockPos pos) {
        return new ArrayList<>(Arrays.asList(
                pos.south(),
                pos.south().west(),
                pos.west(),
                pos.north().west(),
                pos.north(2).west(),
                pos.north(2),
                pos.north(2).east(),
                pos.north(2).east(2),
                pos.north().east(2),
                pos.east(2),
                pos.south().east(2),
                pos.south().east()
        ));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);
        if (!this.cubeUUIDs.isEmpty()) {
            nbt.putUUID("CubeUUID1", this.cubeUUIDs.get(0));
        }
        if (this.cubeUUIDs.size() >= 2) {
            nbt.putUUID("CubeUUID2", this.cubeUUIDs.get(1));
        }
        nbt.putBoolean("Powered", this.wasPowered);
        nbt.putInt("OpenTicks", this.openTicks);
        return nbt;
    }

    @Override
    public void load(BlockState blockState, CompoundNBT nbt) {
        super.load(blockState, nbt);
        this.cubeUUIDs.clear();
        if (nbt.hasUUID("CubeUUID1")) {
            this.cubeUUIDs.add(nbt.getUUID("CubeUUID1"));
        }
        if (nbt.hasUUID("CubeUUID2")) {
            this.cubeUUIDs.add(nbt.getUUID("CubeUUID2"));
        }
        this.wasPowered = nbt.getBoolean("Powered");
        this.openTicks = nbt.getInt("OpenTicks");
    }
}
