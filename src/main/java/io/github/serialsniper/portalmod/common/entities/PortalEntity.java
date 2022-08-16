package io.github.serialsniper.portalmod.common.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.render.entity.PortalEntityRenderer;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import io.github.serialsniper.portalmod.core.init.EntityInit;
import io.github.serialsniper.portalmod.core.init.PacketInit;
import io.github.serialsniper.portalmod.core.packet.EntityTeleportClientPacket;
import io.github.serialsniper.portalmod.core.packet.RadioUpdateClientPacket;
import io.github.serialsniper.portalmod.core.util.BlockInteractor;
import io.github.serialsniper.portalmod.core.util.PortalPair;
import io.github.serialsniper.portalmod.core.util.PortalPairManager;
import io.github.serialsniper.portalmod.mixins.AbstractMinecartEntityAccessor;
import io.github.serialsniper.portalmod.mixins.ActiveRenderInfoAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class PortalEntity extends HangingEntity implements IEntityAdditionalSpawnData {
    private PortalEnd end = PortalEnd.NONE;
    private Direction up = Direction.UP;
    private UUID gunUUID;

    public PortalEntity(EntityType<? extends PortalEntity> type, World level) {
        super(type, level);
    }

    public PortalEntity(World level) {
        super(EntityInit.PORTAL.get(), level);
    }

    @Override
    public void tick() {
        super.tick();
        
        teleportEntities();
        
        if(this.isAlive() && !this.survives())
            this.remove();
    }
    
    private void teleportEntities() {
        if(!this.isOpen())
            return;
        
        PortalPair pair = PortalPairManager.CLIENT.get(this.gunUUID);
        if(pair == null)
            return;

        PortalEntity targetPortal = pair.get(this.end.other());
        if(targetPortal == null)
            return;
        
        Vector3f normal = this.direction.step();
        Vector3f normal2 = this.direction.step();
        normal.mul(.5f);
        normal2.mul(PortalEntityRenderer.OFFSET * 10);
        Vector3d portalPos = Vector3d.atCenterOf(this.blockPosition())
                .subtract(new Vector3d(normal)).add(new Vector3d(normal2));
        
        Vector3f targetnormal = targetPortal.direction.step();
        Vector3f targetnormal2 = targetPortal.direction.step();
        targetnormal.mul(.5f);
        targetnormal2.mul(PortalEntityRenderer.OFFSET * 10);
        Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));
        
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox());
        for(Entity entity : entities) {
            Vector3d distance = entity.position().subtract(portalPos);
            
            if(distance.dot(new Vector3d(this.direction.step())) < 0) {
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                
                Vector3d move = targetPortalPos.subtract(portalPos);
                Vector3d pos = entity.position().add(move);
                entity.setPos(pos.x, pos.y, pos.z);
                entity.xo += move.x;
                entity.yo += move.y;
                entity.zo += move.z;
                entity.xOld += move.x;
                entity.yOld += move.y;
                entity.zOld += move.z;
                
//                if(!this.level.isClientSide && false) {
//                    PacketInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
//                            new EntityTeleportClientPacket(entity.getId(), move));
//                }
                
//                if(entity instanceof AbstractMinecartEntity) {
//                    System.out.println("hasdw");
//                    AbstractMinecartEntityAccessor minecart = (AbstractMinecartEntityAccessor)(AbstractMinecartEntity)entity;
//                    minecart.portalmod_setLX(minecart.portalmod_getLX() + move.x);
//                    minecart.portalmod_setLY(minecart.portalmod_getLY() + move.y);
//                    minecart.portalmod_setLZ(minecart.portalmod_getLZ() + move.z);
//                }
            }
        }
    }
    
    public static void teleportCamera(final EntityViewRenderEvent.CameraSetup event) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        ActiveRenderInfo camera = event.getInfo();
        
        if(minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        
        List<Entity> entities = minecraft.level.getEntities(player, player.getBoundingBox());
        for(Entity entity : entities) {
            if(entity instanceof PortalEntity) {
                if(!((PortalEntity)entity).isOpen())
                    continue;
//                {
//                    PortalEntity portal = (PortalEntity)entity;
//                    Vec3 normal = new Vec3(portal.direction.step());
//                    Vec3 portalPos = new Vec3(portal.blockPosition()).add(.5)
//                            .sub(normal.clone().mul(.5))
//                            .add(normal.clone().mul(PortalEntityRenderer.OFFSET));
//                    
//                    PortalEntity targetPortal = PortalPairManager.CLIENT.get(portal.gunUUID).get(portal.end.other());
//                    Vec3 targetNormal = new Vec3(targetPortal.direction.step());
//                    Vec3 targetPortalPos = new Vec3(targetPortal.blockPosition()).add(.5)
//                            .sub(targetNormal.clone().mul(.5))
//                            .add(targetNormal.clone().mul(PortalEntityRenderer.OFFSET));
//                    
//                    Vec3 cameraPos = new Vec3(camera.getPosition());
//                    
//                    MatrixStack bobStack = new MatrixStack();
//                    minecraft.gameRenderer.bobHurt(bobStack, (float)event.getRenderPartialTicks());
//                    if(minecraft.options.bobView)
//                        minecraft.gameRenderer.bobView(bobStack, (float)event.getRenderPartialTicks());
//                    Mat4 bob = new Mat4(bobStack.last().pose());
//                    
//                    Mat4 view = Mat4.IDENTITY
//                            .mul(Vector3f.XP.rotationDegrees(camera.getXRot()))
//                            .mul(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F))
//                            .translate(cameraPos.clone().negate());
//                   
//                    Vec3 portalPosBob = portalPos.clone().transform(view).transform(bob);
//                    Vec3 normalBob = portalPos.clone().add(normal.clone().negate()).transform(view).transform(bob).sub(portalPosBob);
//                    
//                    if(portalPosBob.dot(normalBob) < 0)
//                        ((ActiveRenderInfoAccessor)event.getInfo()).portalmod_setPosition(cameraPos.add(targetPortalPos.sub(portalPos)).to3d());
//                }
                
                PortalEntity portal = (PortalEntity)entity;
                Vector3f normal = portal.direction.step();
                Vector3f normal2 = portal.direction.step();
                normal.mul(.5f);
                normal2.mul(PortalEntityRenderer.OFFSET);
                Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
                        .subtract(new Vector3d(normal)).add(new Vector3d(normal2));
                
                PortalEntity targetPortal = PortalPairManager.CLIENT.get(portal.gunUUID).get(portal.end.other());
                Vector3f targetNormal = targetPortal.direction.step();
                Vector3f targetnormal2 = targetPortal.direction.step();
                targetNormal.mul(.5f);
                targetnormal2.mul(PortalEntityRenderer.OFFSET);
                Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                        .subtract(new Vector3d(targetNormal)).add(new Vector3d(targetnormal2));
                
                Vector3d cameraPos = camera.getPosition();
                
                MatrixStack bob = new MatrixStack();
                minecraft.gameRenderer.bobHurt(bob, (float)event.getRenderPartialTicks());
                if(minecraft.options.bobView)
                   minecraft.gameRenderer.bobView(bob, (float)event.getRenderPartialTicks());
                Matrix4f bob4f = bob.last().pose();
                
                MatrixStack view = new MatrixStack();
                view.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
                view.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
                view.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                Matrix4f view4f = view.last().pose();
                
                Vector4f portalPosBobbed4f = new Vector4f(new Vector3f(portalPos));
                portalPosBobbed4f.transform(view4f);
                portalPosBobbed4f.transform(bob4f);
                Vector3d portalPosBobbed = new Vector3d(portalPosBobbed4f.x(), portalPosBobbed4f.y(), portalPosBobbed4f.z());
                
                Vector3f normal3 = portal.direction.getOpposite().step();
                normal3.add(new Vector3f(portalPos));
                Vector4f normalBobbed4f = new Vector4f(normal3);
                normalBobbed4f.transform(view4f);
                normalBobbed4f.transform(bob4f);
                Vector3d normalBobbed = new Vector3d(normalBobbed4f.x(), normalBobbed4f.y(), normalBobbed4f.z());
                normalBobbed = normalBobbed.subtract(portalPosBobbed);
                
                if(portalPosBobbed.dot(normalBobbed) < 0)
                    ((ActiveRenderInfoAccessor)event.getInfo()).portalmod_setPosition(cameraPos.add(targetPortalPos.subtract(portalPos)));
            }
        }
    }

    private Pair<BlockPos, BlockPos> getFrontBlockPoses() {
        BlockPos front = blockPosition();
        BlockPos frontUp = front.relative(up);
        return new Pair<BlockPos, BlockPos>(front, frontUp);
    }

    private Pair<BlockPos, BlockPos> getHindBlockPoses() {
        BlockPos hind = blockPosition().relative(getDirection().getOpposite());
        BlockPos hindUp = hind.relative(up);
        return new Pair<BlockPos, BlockPos>(hind, hindUp);
    }

    private Pair<BlockState, BlockState> getBlockStates(Pair<BlockPos, BlockPos> poses) {
        return new Pair<BlockState, BlockState>(level.getBlockState(poses.getFirst()),
                level.getBlockState(poses.getSecond()));
    }

    private void updateBlocksBehind() {
        Pair<BlockPos, BlockPos> poses = getHindBlockPoses();
        Pair<BlockState, BlockState> states = getBlockStates(poses);
        level.sendBlockUpdated(poses.getFirst(), states.getFirst(), states.getFirst(), 0);
        level.sendBlockUpdated(poses.getSecond(), states.getSecond(), states.getSecond(), 0);
    }

    protected void recalculateBoundingBox() {
        if(this.direction == null || this.up == null)
            return;

        Vector3d pos = Vector3d.atCenterOf(this.pos)
                .subtract(new Vector3d(this.direction.step()).multiply(.49f, .49f, .49f));
        this.setPosRaw(pos.x, pos.y, pos.z);

        float thickness = 1f / 16f;
        Vector4f baseVertex = new Vector4f(0, 0, 0, 1);
        Vector4f endVertex = new Vector4f(1, 2, thickness, 1);

        MatrixStack matrix = new MatrixStack();
        setupMatrix(matrix, this.direction, this.up, getPivotPoint());
        baseVertex.transform(matrix.last().pose());
        endVertex.transform(matrix.last().pose());

        this.setBoundingBox(new AxisAlignedBB(baseVertex.x(), baseVertex.y(), baseVertex.z(), endVertex.x(),
                endVertex.y(), endVertex.z()));
    }

    public Vector3d getPivotPoint() {
        return Vector3d.atCenterOf(this.pos.relative(this.direction.getOpposite()));
    }

    public static void setupMatrix(MatrixStack matrix, Direction direction, Direction upVector, Vector3d center) {
        int i = direction.getStepY() * -1;
        float yRot;

        if(direction.getAxis().isHorizontal()) {
            yRot = -direction.toYRot();
        } else {
            yRot = -upVector.getOpposite().toYRot();
            if(direction.getAxisDirection() == AxisDirection.NEGATIVE)
                yRot += 180;
        }

        matrix.translate(center.x, center.y, center.z);
        matrix.mulPose(Vector3f.YP.rotationDegrees(yRot));
        matrix.mulPose(Vector3f.XP.rotationDegrees(i * 90));
        matrix.translate(-.5f, -.5f, .5f);
    }
    
    @Nullable
    public PortalEntity getOtherEnd() {
        return PortalPairManager.CLIENT.get(this.gunUUID).get(this.end.other());
    }
    
    public Vector3f getNormal() {
        return this.direction.step();
    }
    
    public Vector3d getCenter() {
        Vector3f normal = this.getNormal();
        normal.mul(.5f);
        return this.getPivotPoint().add(new Vector3d(normal));
    }
    
    public Vector3d getRenderOffset() {
        PortalEntity targetPortal = this.getOtherEnd();
//        if(targetPortal == null)
            return Vector3d.ZERO;
//        return targetPortal.getCenter().subtract(this.getCenter());
    }

    public void onReplaced() {
        this.remove();
    }

    public boolean isOpen() {
        PortalPair pair = PortalPairManager.select(this.level).get(this.gunUUID);
        if(pair == null)
            return false;
        return pair.isFull();
    }

    public void setEnd(PortalEnd end) {
        this.end = end;
    }

    public PortalEnd getEnd() {
        return this.end;
    }

    public void setGunUUID(UUID gunUUID) {
        this.gunUUID = gunUUID;
    }

    public UUID getGunUUID() {
        return this.gunUUID;
    }

    public void setUpVector(Direction upVector) {
        this.up = upVector;
    }

    public Direction getUpVector() {
        return this.up;
    }

    public void setDirection(@Nonnull Direction direction) {
        this.direction = direction;

        if(direction.getAxis().isHorizontal()) {
            this.xRot = 0.0F;
            this.yRot = (float) (this.direction.get2DDataValue() * 90);
        } else {
            this.xRot = (float) (-90 * direction.getAxisDirection().getStep());
            this.yRot = 0.0F;
        }

        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
        this.recalculateBoundingBox();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

//        updateBlocksBehind();

        if(!this.level.isClientSide) {
            ForgeChunkManager.forceChunk((ServerWorld) this.level, PortalMod.MODID, this, this.xChunk, this.zChunk, true, true);
            PortalPairManager.SERVER.put(gunUUID, end, this);
        }
    }

    public boolean survives() {
        return survives(this.level);
    }

    public boolean survives(World level) {
        Vector3f normal = up.step();
        normal.mul(.5f);
        
        if(getPortals(this.position().add(new Vector3d(normal)), .1f, portal -> portal != this && portal.isAlive()).size() > 0)
            return false;
        
        if(true)
            return true;
        
        return new BlockInteractor(this.level, this.blockPosition(), true)
//        .and(b -> b == Blocks.AIR)
//        .and(up, b -> b == Blocks.AIR)
        .move(this.direction.getOpposite())
        .and(b -> b == BlockInit.PORTALABLE_BLOCK.get())
        .and(up, b -> b == BlockInit.PORTALABLE_BLOCK.get())
        .getResult();
    }
    
    private enum Intersection {
        UP, DOWN, BOTH, NONE
    }
    
    private Intersection getIntersection(PortalEntity portal, @Nullable Direction direction, boolean skipSame) {
        if(skipSame && this.equals(portal))
            return Intersection.NONE;
        
        BlockPos pos = this.blockPosition();
        BlockPos targetPos = portal.blockPosition();
        Intersection result = Intersection.NONE;
        
        if(direction != null)
            pos = pos.relative(direction);
        
        if(portal.direction != this.direction)
            return result;
        
        if(pos.equals(targetPos) || pos.equals(targetPos.relative(portal.up)))
            result = Intersection.DOWN;
        
        pos = pos.relative(this.up);
        if(pos.equals(targetPos) || pos.equals(targetPos.relative(portal.up)))
            result = (result == Intersection.DOWN ? Intersection.BOTH : Intersection.UP);
        
        return result;
    }
    
    private boolean intersects(List<PortalEntity> portals) {
        for(PortalEntity portal : portals)
            if(this.getIntersection(portal, null, true) != Intersection.NONE)
                return true;
        return false;
    }
    
    private void setPos(BlockPos pos) {
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }
    
    private void movePos(Direction direction) {
        this.setPos(this.blockPosition().relative(direction));
    }
    
    private boolean equals(PortalEntity portal) {
        return portal.gunUUID.equals(this.gunUUID) && portal.end == this.end;
    }
    
    public boolean adjustShot(BlockRayTraceResult ray) {
        BlockInteractor interactor = new BlockInteractor(this.level, this.blockPosition(), true)
        .and(direction.getOpposite(), b -> b == BlockInit.PORTALABLE_BLOCK.get());
        
        if(!interactor.getResult())
            return false;
        
        List<PortalEntity> portals = getPortals(this.position(), 2, portal ->
            portal != this && portal.isAlive() && portal.getDirection() == this.direction);
        
        for(PortalEntity portal : portals) {
            switch(this.getIntersection(portal, null, true)) {
                case UP:
                    this.movePos(up.getOpposite());
                    if(this.intersects(portals))
                        return false;
                    break;
                    
                case DOWN:
                case BOTH:
                    Vector3d offset = ray.getLocation().subtract(Vector3d.atCenterOf(ray.getBlockPos()));
                    
                    Vector3f targetLeftNormal = portal.direction.step();
                    targetLeftNormal.cross(portal.up.step());
                    Direction targetLeft = Direction.fromNormal((int)targetLeftNormal.x(), (int)targetLeftNormal.y(), (int)targetLeftNormal.z());
                    
                    // todo use abs
                    double x = targetLeft.getAxis().choose(offset.x, offset.y, offset.z)
                            * (targetLeft.getAxisDirection() == AxisDirection.NEGATIVE ? 1 : -1);
                    double y = portal.up.getAxis().choose(offset.x, offset.y, offset.z)
                            * (portal.up.getAxisDirection() == AxisDirection.NEGATIVE ? -1 : 1);
                    
                    boolean invert = blockPosition().equals(portal.blockPosition());
                    
                    if(invert)
                        y *= -1;
                    
                    if(y > Math.abs(x)) {
                        Direction portalUp = portal.up;
                        
                        if(invert)
                            portalUp = portalUp.getOpposite();
                        
//                        if(this.getIntersection(portal, null, true) == Intersection.BOTH) {
//                            this.setPos(this.blockPosition().relative(portalUp).relative(portalUp));
//                        } else {
//                            this.movePos(portalUp);
//                        }
                        
                        this.movePos(portalUp);
                        if(this.getIntersection(portal, null, true) != Intersection.NONE)
                            this.movePos(portalUp);
                    } else if(x >= 0) {
                        this.movePos(targetLeft.getOpposite());
                        if(this.getIntersection(portal, null, true) != Intersection.NONE)
                            this.movePos(targetLeft.getOpposite());
                    } else {
                        this.movePos(targetLeft);
                        if(this.getIntersection(portal, null, true) != Intersection.NONE)
                            this.movePos(targetLeft);
                    }
                    
                    break;
                    
                case NONE:
                    continue;
            }
            
            break;
        }
        
        BlockInteractor blockPush = new BlockInteractor(this.level, this.blockPosition(), false)
        .move(up)
        .or(b -> b != Blocks.AIR)
        .move(direction.getOpposite())
        .or(b -> b != BlockInit.PORTALABLE_BLOCK.get());
        
        if(blockPush.getResult()) {
            blockPush
            .and((pos, state, block) -> {
                this.movePos(up.getOpposite());
                return !this.intersects(portals);
            });
            
            if(!blockPush.getResult())
                return false;
        }
        
        BlockInteractor blockCheck = new BlockInteractor(this.level, this.blockPosition(), true)
        .and(b -> b == Blocks.AIR)
        .and(up, b -> b == Blocks.AIR)
        .move(direction.getOpposite())
        .and(b -> b == BlockInit.PORTALABLE_BLOCK.get())
        .and(up, b -> b == BlockInit.PORTALABLE_BLOCK.get())
        .and((pos, state, block) -> {
            for(PortalEntity portal : portals)
                if(this.getIntersection(portal, null, false) == Intersection.BOTH
                    && this.up == portal.up)
                    return false;
            return true;
        });
        
        return blockCheck.getResult();
    }

    private List<PortalEntity> getPortals(Vector3d pos, float size, Predicate<PortalEntity> predicate) {
        try {
            return this.level.getEntitiesOfClass(this.getClass(), new AxisAlignedBB(pos, pos).inflate(size), predicate);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<PortalEntity>();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        updateBlocksBehind();
        PortalPairManager.select(this.level).remove(gunUUID, this);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    public void push(double x, double y, double z) {
    }

    public boolean hurt(DamageSource source, float amount) {
        return source != DamageSource.OUT_OF_WORLD ? false : super.hurt(source, amount);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderBlockOverlay(World level, BlockPos pos) {
        try {
            List<Entity> portals = level.getEntitiesOfClass(PortalEntity.class, new AxisAlignedBB(pos).inflate(1),
                    portal -> {
                        Pair<BlockPos, BlockPos> poses = ((PortalEntity) portal).getHindBlockPoses();
                        return ((PortalEntity) portal).isOpen()
                                && (poses.getFirst().equals(pos) || poses.getSecond().equals(pos));
                    });

            if(portals.size() > 0)
                return false;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void dropItem(@Nullable Entity entity) {
//        this.playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
    }

    public void playPlacementSound() {
//        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
    }

    @Override
    protected float getEyeHeight(Pose p_213316_1_, EntitySize p_213316_2_) {
        return 0.0F;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public ActionResultType interact(PlayerEntity p_184230_1_, Hand p_184230_2_) {
        return ActionResultType.SUCCESS;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeUUID(this.gunUUID).writeEnum(this.end).writeByte((byte) this.up.get3DDataValue())
                .writeByte((byte) this.direction.get3DDataValue());
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.gunUUID = buffer.readUUID();
        this.end = buffer.readEnum(PortalEnd.class);
        this.up = Direction.from3DDataValue(buffer.readByte());
        this.setDirection(Direction.from3DDataValue(buffer.readByte()));

        PortalPairManager.CLIENT.put(this.gunUUID, this.end, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("end", this.end.getSerializedName());
        nbt.putString("facing", this.direction.getSerializedName().toUpperCase());
        nbt.putString("up", this.up.getSerializedName().toUpperCase());
        nbt.putUUID("gunUUID", this.gunUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        this.end = PortalEnd.valueOf(nbt.getString("end").toUpperCase());
        this.up = Direction.valueOf(nbt.getString("up"));
        this.setDirection(Direction.valueOf(nbt.getString("facing")));
        this.gunUUID = nbt.getUUID("gunUUID");
    }
}