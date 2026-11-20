package net.portalmod.common.sorted.faithplate;

import com.mojang.math.Axis;
import net.portalmod.core.network.PacketDistributor;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.common.sorted.portal.VolatilePortalHelperManager;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static net.portalmod.common.sorted.faithplate.FaithPlateBlock.*;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;
import net.minecraft.world.level.Level;

public class FaithPlateTileEntity extends BlockEntity implements IndicatorActivated {
    private boolean enabled = false;
    private boolean indicatorControlled = false;
    private boolean override = false;
    private net.minecraft.world.phys.Vec3 targetPos;
    private Direction targetFace;
    private Float height;
    private int cooldown = 0;
    public static int COOLDOWN_DURATION = 10;

    private static final HashMap<Player, FaithPlateTileEntity> PLATE_PER_PLAYER = new HashMap<>();
    private Player configuringPlayer;

    private static final VoxelShapeGroup TRIGGER = new VoxelShapeGroup.Builder()
            .add(3, 16, 3, 13, 17, 13)
            .build();

    public FaithPlateTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    public FaithPlateTileEntity(BlockPos pos, BlockState state) {
        this(TileEntityTypeInit.FAITHPLATE.get(), pos, state);
    }
    
    public void tick() {
        if(this.level == null)
            return;

        // Add target portal helper
        if(!this.level.isClientSide && targetPos != null) {
            Vec3 helperPosition = new Vec3(targetPos).add(this.worldPosition).add(.5).add(new Vec3(this.targetFace).mul(.5));
            VolatilePortalHelperManager.getInstance().addVolatilePortalHelper(this.level, helperPosition, this.targetFace, 1);
        }

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
            if(entity.isPassenger())
                continue;

            if(entity instanceof Player) {
                Player player = (Player)entity;
                if(player.getAbilities().flying)
                    continue;
            }

            this.launchEntity(entity);
        }
    }

    private void launchEntity(Entity entity) {
        if(this.level == null)
            return;

        net.minecraft.world.phys.Vec3 targetPos = getTargetPos();

        double offset = getTargetFace().getAxis().isHorizontal() || getTargetFace() == Direction.DOWN
                ? -0.25 - 0.5 * entity.getBbHeight()
                : 0;

        Vec3 target = new Vec3(targetPos).add(.5).add(new Vec3(getTargetFace().getNormal()).mul(.5));
        Vec3 relativeTarget = target.clone().add(getBlockPos()).sub(entity.position()).add(0, offset, 0);

        if(targetPos.x() == 0 && targetPos.z() == 0)
            relativeTarget = new Vec3(getBlockPos()).add(.5, 1, .5).sub(entity.position());

        FaithPlateParabola parabola = new FaithPlateParabola(relativeTarget.to3d(), height);
        double angle = parabola.getAngle();
        double velocity = parabola.getVelocity();
        double rotation = parabola.getRotation();

        entity.setDeltaMovement(new net.minecraft.world.phys.Vec3(
                velocity * Math.cos(angle) * Math.cos(rotation),
                velocity * Math.sin(angle),
                velocity * Math.cos(angle) * Math.sin(rotation)
        ));

        entity.setShiftKeyDown(false);
        ((Flingable)entity).setFlinging(true);
        this.cooldown = COOLDOWN_DURATION;

        if(!this.level.isClientSide) {
            if(entity.isControlledByLocalInstance() && !(entity instanceof Player)) {
                PacketInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(
                        () -> entity), new SFaithPlateLaunchPacket(getBlockPos()));
            }
        } else {
            PacketInit.INSTANCE.sendToServer(new CFaithPlateLaunchPacket(getBlockPos()));
        }
    }

    public void startConfiguration(ServerPlayer player) {
        this.configuringPlayer = player;
        PLATE_PER_PLAYER.put(player, this);
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SFaithPlateStartConfigPacket(this.getBlockPos()));
    }

    public void endConfiguration() {
        endConfigurationForPlayer(this.configuringPlayer);
    }

    public static void endConfigurationForPlayer(Player player) {
        if(PLATE_PER_PLAYER.containsKey(player))
            PLATE_PER_PLAYER.get(player).configuringPlayer = null;
        PLATE_PER_PLAYER.remove(player);
    }

    public static boolean isPlayerConfiguring(Player player) {
        return PLATE_PER_PLAYER.containsKey(player);
    }

    // todo handle player leave
    public boolean isBeingConfigured() {
        return this.configuringPlayer != null;
    }

    public AABB getTrigger() {
        BlockState state = this.getBlockState();
        VoxelShapeGroup triggerTransformed = TRIGGER.clone();

        if(state.getValue(FACE) == Face.WALL) {
            Mat4 matrix = Mat4.identity()
            .translate(new Vec3(.5))
            .rotateDeg(Axis.YP, -state.getValue(FACING).toYRot())
            .rotateDeg(Axis.XP, 90)
            .translate(new Vec3(-.5));

            triggerTransformed.transform(matrix);
        }

        AABB aabb = triggerTransformed.getShape().bounds();

        FaithPlateBlock block = (FaithPlateBlock) state.getBlock();
        BlockPos mainPosition = block.getMainPosition(state, this.getBlockPos());

        return Shapes.or(
                Shapes.create(aabb.move(mainPosition)),
                Shapes.create(aabb.move(mainPosition.relative(block.getUpperDirection(state))))
        ).bounds();
    }
    
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        if(targetPos != null && targetFace != null) {
            CompoundTag target = new CompoundTag();
            target.putDouble("x", targetPos.x());
            target.putDouble("y", targetPos.y());
            target.putDouble("z", targetPos.z());
            target.putByte("side", (byte) targetFace.get3DDataValue());
            target.putFloat("height", height);
            nbt.put("target", target);
        }
        
        nbt.putBoolean("enabled", enabled);
        super.saveAdditional(nbt);
    }
    
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        loadFields(nbt);
    }
    
    public void loadFields(CompoundTag nbt) {
        enabled = false;
        
        if(nbt.contains("enabled"))
            enabled = nbt.getBoolean("enabled");
        
        if(nbt.contains("target")) {
            CompoundTag target = nbt.getCompound("target");
            if(target.contains("x")
                    && target.contains("y")
                    && target.contains("z")
                    && target.contains("side")
                    && target.contains("height")) {
                
                double x = target.getDouble("x");
                double y = target.getDouble("y");
                double z = target.getDouble("z");
                targetPos = new net.minecraft.world.phys.Vec3(x, y, z);
                targetFace = Direction.from3DDataValue(target.getByte("side"));
                height = target.getFloat("height");
            }
        }
    }
    
    public boolean isEnabled() {
        return override;
    }
    
    public net.minecraft.world.phys.Vec3 getTargetPos() {
        return targetPos;
    }
    
    public Direction getTargetFace() {
        return targetFace;
    }
    
    public float getHeight() {
        return height;
    }

    public float getPredictedHeight(net.minecraft.world.phys.Vec3 absoluteTargetPos) {
        if (height != null) return height;
        return Math.max(1, (float) (BlockPos.containing(absoluteTargetPos).distManhattan(this.getBlockPos()) / 4.0));
    }

    public int getCooldown() {
        return cooldown;
    }

    public void rotate(Rotation rotation) {
        net.minecraft.world.phys.Vec3 newTarget = this.targetPos;
        switch(rotation) {
            case CLOCKWISE_90:
                newTarget = new net.minecraft.world.phys.Vec3(-this.targetPos.z, this.targetPos.y, this.targetPos.x); break;
            case CLOCKWISE_180:
                newTarget = new net.minecraft.world.phys.Vec3(-this.targetPos.x, this.targetPos.y, -this.targetPos.z); break;
            case COUNTERCLOCKWISE_90:
                newTarget = new net.minecraft.world.phys.Vec3(this.targetPos.z, this.targetPos.y, -this.targetPos.x); break;
        }
        this.targetPos = newTarget;
        this.targetFace = rotation.rotate(this.targetFace);
    }

    public void mirror(Mirror mirror) {
        this.targetPos = new Vec3(this.targetPos).mul(mirror == Mirror.FRONT_BACK ? -1 : 1, 1, mirror == Mirror.LEFT_RIGHT ? -1 : 1).to3d();
        this.targetFace = mirror.mirror(this.targetFace);
    }

    // chunk update
    
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        load(tag);
    }
    
    // block update
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.load(packet.getTag());
    }
    
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(1E30);
    }
    
    public double getViewDistance() {
        return 256.0D;
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, Level world, BlockPos pos) {
        Direction facing = blockState.getValue(FACING);

        Direction up = Direction.UP;
        Direction feet = facing;
        Direction side = facing.getClockWise();
        boolean vertical = blockState.getValue(FACE) == Face.WALL;
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