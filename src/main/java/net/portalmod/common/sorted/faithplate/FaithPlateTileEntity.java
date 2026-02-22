package net.portalmod.common.sorted.faithplate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.portalmod.common.sorted.faithplate.FaithPlateBlock.*;

public class FaithPlateTileEntity extends TileEntity implements ITickableTileEntity, IndicatorActivated {
    private boolean enabled = false;
    private boolean indicatorControlled = false;
    private boolean override = false;
    private BlockPos targetPos;
    private Direction targetFace;
    private float height;
    private int cooldown = 0;
    public static int COOLDOWN_DURATION = 10;

    private static final VoxelShapeGroup TRIGGER = new VoxelShapeGroup.Builder()
            .add(0, 16, 0, 16, 17, 16)
            .build();

    public FaithPlateTileEntity(TileEntityType<?> type) {
        super(type);
    }
    
    public FaithPlateTileEntity() {
        this(TileEntityTypeInit.FAITHPLATE.get());
    }
    
    @Override
    public void tick() {
        // Check for indicators
        IndicatorInfo indicatorInfo = this.checkIndicators(this.getBlockState(), this.getLevel(), this.getBlockPos());

        this.indicatorControlled = indicatorInfo.hasIndicators;
        if (this.indicatorControlled) {
            this.override = indicatorInfo.allIndicatorsActivated;
        } else {
            this.override = enabled;
        }

        // Keep track of how long it's been since the last launch
        if (cooldown > 0) cooldown--;
        if (targetPos == null || targetFace == null || !override) return;
        if (cooldown > 0) return;


        for(Entity entity : level.getEntitiesOfClass(LivingEntity.class, this.getTrigger())) {
            if (entity.isPassenger()) continue;

            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
                if (player.abilities.flying) continue;
            }

            BlockPos targetPos = getTargetPos();
            
            Vec3 target = new Vec3(targetPos).add(.5).add(new Vec3(getTargetFace().getNormal()).mul(.5));
            Vec3 relativeTarget = target.clone().add(getBlockPos()).sub(entity.position());
            
            if(targetPos.getX() == 0 && targetPos.getZ() == 0)
                relativeTarget = new Vec3(getBlockPos()).add(.5, 1, .5).sub(entity.position());
            
            FaithPlateParabola parabola = new FaithPlateParabola(relativeTarget.to3d(), height);
            double angle = parabola.getAngle();
            double velocity = parabola.getVelocity();
            double rotation = parabola.getRotation();
            
            entity.setDeltaMovement(new Vector3d(
                    velocity * Math.cos(angle) * Math.cos(rotation),
                    velocity * Math.sin(angle),
                    velocity * Math.cos(angle) * Math.sin(rotation)
            ));

            entity.setShiftKeyDown(false);
            ((Flingable)entity).setFlinging(true);

            this.cooldown = COOLDOWN_DURATION;

            if(!level.isClientSide) {
                if(entity.isControlledByLocalInstance() && !(entity instanceof PlayerEntity)) {
                    PacketInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(
                            () -> entity), new SFaithPlateLaunchPacket(getBlockPos()));
                }
            } else {
                PacketInit.INSTANCE.sendToServer(new CFaithPlateLaunchPacket(getBlockPos()));
            }
        }
    }

//    private void initAABBs() {
//        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
//                .add(0, 16, 0, 16, 17, 16)
//                .build();
//
//        for(FaithPlateBlock.Face face : FaithPlateBlock.Face.values()) {
//            for(Direction facing : Direction.values()) {
//                Mat4 matrix = Mat4.identity();
//
//                if(facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
//                    matrix.scale(new Vec3(facing.step()).mul(2).add(1));
//                    matrix.rotateDeg(facing.step(), corner.getRot() - 90);
//                } else {
//                    matrix.rotateDeg(facing.step(), corner.getRot());
//                }
//
//                if(facing.getAxis() == Direction.Axis.X)
//                    matrix.rotateDeg(Vector3f.ZP, -90)
//                            .rotateDeg(Vector3f.YP, 90);
//
//                if(facing.getAxis() == Direction.Axis.Z)
//                    matrix.rotateDeg(Vector3f.XP, 90);
//
//                matrix.rotateDeg(Vector3f.YP, 90);
//                matrix.translate(new Vec3(-.5));
//
//                TRIGGERS.put(facing, corner, shape.clone().transform(matrix));
//            }
//        }
//    }
    
    public AxisAlignedBB getTrigger() {
        BlockState state = this.getBlockState();
        VoxelShapeGroup triggerTransformed = TRIGGER.clone();

        if(state.getValue(FACE) == FaithPlateBlock.Face.WALL) {
            Mat4 matrix = Mat4.identity()
            .translate(new Vec3(.5))
            .rotateDeg(Vector3f.YP, -state.getValue(FACING).toYRot())
            .rotateDeg(Vector3f.XP, 90)
            .translate(new Vec3(-.5));

            triggerTransformed.transform(matrix);
        }

        AxisAlignedBB aabb = triggerTransformed.getShape().bounds();

        return VoxelShapes.or(
                VoxelShapes.create(aabb.move(getBlockPos())),
                VoxelShapes.create(aabb.move(getBlockPos().relative(FaithPlateBlock.getOtherBlockDirection(state))))
        ).bounds();
    }
    
    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        if(targetPos != null && targetFace != null) {
            CompoundNBT target = new CompoundNBT();
            target.putInt("x", targetPos.getX());
            target.putInt("y", targetPos.getY());
            target.putInt("z", targetPos.getZ());
            target.putByte("side", (byte) targetFace.get3DDataValue());
            target.putFloat("height", height);
            nbt.put("target", target);
        }
        
        nbt.putBoolean("enabled", enabled);
        return super.save(nbt);
    }
    
    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        load(nbt);
    }
    
    public void load(CompoundNBT nbt) {
        enabled = false;
        
        if(nbt.contains("enabled"))
            enabled = nbt.getBoolean("enabled");
        
        if(nbt.contains("target")) {
            CompoundNBT target = nbt.getCompound("target");
            if(target.contains("x")
                    && target.contains("y")
                    && target.contains("z")
                    && target.contains("side")
                    && target.contains("height")) {
                
                int x = target.getInt("x");
                int y = target.getInt("y");
                int z = target.getInt("z");
                targetPos = new BlockPos(x, y, z);
                targetFace = Direction.from3DDataValue(target.getByte("side"));
                height = target.getFloat("height");
            }
        }
    }
    
    public boolean isEnabled() {
        return override;
    }
    
    public BlockPos getTargetPos() {
        return targetPos;
    }
    
    public Direction getTargetFace() {
        return targetFace;
    }
    
    public float getHeight() {
        return height;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    public void rotate(Rotation rotation) {
        this.targetPos = this.targetPos.rotate(rotation);
        this.targetFace = rotation.rotate(this.targetFace);
    }

    @Override
    public void mirror(Mirror mirror) {
        this.targetPos = new Vec3(this.targetPos).mul(mirror == Mirror.FRONT_BACK ? -1 : 1, 1, mirror == Mirror.LEFT_RIGHT ? -1 : 1).toBlockPos();
        this.targetFace = mirror.mirror(this.targetFace);
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
        return new AxisAlignedBB(this.getBlockPos()).inflate(1E30);
    }
    
    @Override
    public double getViewDistance() {
        return 256.0D;
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, World world, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);

        Direction up = Direction.UP;
        Direction feet = facing;
        Direction side = facing.getClockWise();
        boolean vertical = blockState.getValue(FACE) == FaithPlateBlock.Face.WALL;
        boolean topHalf  = blockState.getValue(HALF) == DoubleBlockHalf.UPPER;

        if (vertical) {
            up = facing;
            feet = Direction.DOWN;
        }
        if (!topHalf) feet = feet.getOpposite();

        BlockPos above = pos.relative(up);
        return new ArrayList<>(Arrays.asList(
                above.relative(feet, 2).relative(side.getOpposite()),
                above.relative(feet, 2),
                above.relative(feet, 2).relative(side),

                above.relative(feet).relative(side),
                above.relative(side),

                above.relative(feet.getOpposite()).relative(side),
                above.relative(feet.getOpposite()),
                above.relative(feet.getOpposite()).relative(side.getOpposite()),

                above.relative(side.getOpposite()),
                above.relative(feet).relative(side.getOpposite())
        ));
    }

    public boolean isIndicatorControlled() {
        return indicatorControlled;
    }

    public void setIndicatorControlled(boolean indicatorControlled) {
        this.indicatorControlled = indicatorControlled;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}