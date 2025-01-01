package net.portalmod.common.sorted.pellet;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.packet.SSpawnPelletPacket;

public class PelletEntity extends DamagingProjectileEntity implements IRendersAsItem {

    public static final double SPEED = 0.2;

    public PelletEntity(EntityType<? extends DamagingProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public PelletEntity(World world, double x, double y, double z, double dx, double dy, double dz) {
        super(EntityInit.PELLET.get(), x, y, z, dx, dy, dz, world);
    }

    @Override
    public void tick() {
        // fireballs are weird
        this.xPower = 0;
        this.yPower = 0;
        this.zPower = 0;

        // ignore friction
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(SPEED));

        super.tick();
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
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ItemInit.TEST_BLOCK.get());
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        Vector3d position = this.position();
        Vector3d movement = this.getDeltaMovement();
        return new SSpawnPelletPacket(this.getId(), this.getUUID(), position.x, position.y, position.z, movement.x, movement.y, movement.z);
    }
}
