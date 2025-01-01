package net.portalmod.common.sorted.pellet;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.packet.SSpawnPelletPacket;
import net.portalmod.core.util.ModUtil;

public class PelletEntity extends ProjectileEntity {

    public static final double SPEED = 0.15;

    public PelletEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public PelletEntity(World world, double x, double y, double z, double dx, double dy, double dz) {
        this(EntityInit.PELLET.get(), world);
        this.moveTo(x, y, z);
        this.setDeltaMovement(dx, dy, dz);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        RayTraceResult raytraceresult = ProjectileHelper.getHitResult(this, this::canHitEntity);
        if (raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            this.onHit(raytraceresult);
        }

        this.checkInsideBlocks();

        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(SPEED));

        Vector3d newPos = this.position().add(this.getDeltaMovement());
        this.setPos(newPos.x, newPos.y, newPos.z);

//        ModUtil.sendChat(level, this.getDeltaMovement() + (level.isClientSide ? "CLIENT" : "SERVER"));
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {
        super.onHitBlock(result);

        Direction.Axis bounceAxis = result.getDirection().getAxis();
        Vector3d delta = this.getDeltaMovement();
        if (bounceAxis == Direction.Axis.X) delta = delta.multiply(-1, 1, 1);
        if (bounceAxis == Direction.Axis.Y) delta = delta.multiply(1, -1, 1);
        if (bounceAxis == Direction.Axis.Z) delta = delta.multiply(1, 1, -1);
        this.setDeltaMovement(delta);
        ModUtil.sendChat(level, level.isClientSide ? "CLIENT BOUNCE" : "SERVER BOUNCE");
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isPickable() {
//        return false;
        return true;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        Vector3d position = this.position();
        Vector3d movement = this.getDeltaMovement();
        return new SSpawnPelletPacket(this.getId(), this.getUUID(), position.x, position.y, position.z, movement.x, movement.y, movement.z);
    }
}
