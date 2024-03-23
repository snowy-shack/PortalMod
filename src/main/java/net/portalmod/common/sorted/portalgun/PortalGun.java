package net.portalmod.common.sorted.portalgun;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.core.init.*;
import net.portalmod.core.math.Vec3;

import java.util.List;
import java.util.UUID;

public class PortalGun extends Item {
    public PortalGun(Properties properties) {
        super(properties);
    }

    public static void handleLeftClick() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PortalGunClient::handleLeftClick);
    }
    
    public static void handleRightClick() {
        PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.ORANGE).build());
    }
    
    public static void dropCube(PlayerEntity player, boolean toBeThrown) {
        List<Entity> cubes = player.getPassengers();
        for(int i = cubes.size() - 1; i >= 0; --i) {
            Entity cube = cubes.get(0);
            if(cube instanceof Cube) {
                cube.stopRiding();
                if(toBeThrown) {
                    float strength = .2f;
                    cube.setDeltaMovement(cube.getDeltaMovement().add(player.getViewVector(0)
                            .multiply(strength, strength, strength)));
                }
            }
        }
    }

    private static BlockRayTraceResult customClip(World level, RayTraceContext context) {
        return IBlockReader.traverseBlocks(context, (ctx, pos) -> {
            BlockState blockstate = level.getBlockState(pos);
            if(blockstate.is(BlockTagInit.PORTAL_TRANSPARENT))
                return null;
            FluidState fluidstate = level.getFluidState(pos);
            Vector3d vector3d = ctx.getFrom();
            Vector3d vector3d1 = ctx.getTo();
            VoxelShape voxelshape = ctx.getBlockShape(blockstate, level, pos);
            BlockRayTraceResult blockraytraceresult = level.clipWithInteractionOverride(vector3d, vector3d1, pos, voxelshape, blockstate);
            VoxelShape voxelshape1 = ctx.getFluidShape(fluidstate, level, pos);
            BlockRayTraceResult blockraytraceresult1 = voxelshape1.clip(vector3d, vector3d1, pos);
            double d0 = blockraytraceresult == null ? Double.MAX_VALUE : ctx.getFrom().distanceToSqr(blockraytraceresult.getLocation());
            double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : ctx.getFrom().distanceToSqr(blockraytraceresult1.getLocation());
            return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
        }, ctx -> {
            Vector3d vector3d = ctx.getFrom().subtract(ctx.getTo());
            return BlockRayTraceResult.miss(ctx.getTo(), Direction.getNearest(vector3d.x, vector3d.y, vector3d.z), new BlockPos(ctx.getTo()));
        });
    }
    
    public static void placePortal(PlayerEntity player, World level, PortalEnd end, ItemStack gun) {
        Vector3d rayPath = player.getViewVector(0).scale(200);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);
        BlockRayTraceResult ray = customClip(level, rayCtx);
        
        if(ray.getType() == RayTraceResult.Type.MISS && !level.isClientSide) {
            double timeOfDay = (level.getTimeOfDay(0) + .25f);
            double angle = (timeOfDay - (int)timeOfDay) * 2f * Math.PI;
            Vector3d moonVector = new Vector3d(Math.cos(angle), Math.sin(angle), 0);
            double dot = player.getLookAngle().dot(moonVector);
            if(dot <= -0.997)
                CriteriaTriggerInit.SHOOT_MOON.get().trigger((ServerPlayerEntity)player);
        }
        
        Direction face = ray.getDirection();
        BlockPos pos = ray.getBlockPos().relative(face);
        PortalEntity portal = new PortalEntity(level);
        
        if(!face.getAxis().isHorizontal()) {
            Direction upVector = player.getDirection();
            portal.setUpVector(upVector);
        }

        // todo .5
//        Vec3 portalPos = new Vec3(pos).add(.5).sub(new Vec3(face.getNormal()).mul(.499));
        Vec3 portalPos = new Vec3(pos).add(.5).sub(new Vec3(face.getNormal()).mul(.5));
        portal.setPos(portalPos.x, portalPos.y, portalPos.z);
        portal.setDirection(face);
        portal.setEnd(end);
        portal.setGunUUID(getUUID(gun));

        CompoundNBT nbt = gun.getOrCreateTag();
        if(nbt.contains("portalHue"))
            portal.setHue(nbt.getString("portalHue"));

//        portal.setOtherPortalPos();
        if(!portal.adjustShot(ray))
            return;

        // todo check if this is client or server side

        boolean allQualityBlocks = true;
        for(BlockPos blockPos : portal.getBlocksBehind()) {
            // todo check any quality block
            if(portal.level.getBlockState(blockPos).getBlock() != BlockInit.LUNECAST.get())
                allQualityBlocks = false;
        }

        if(allQualityBlocks)
            CriteriaTriggerInit.PORTAL_SURFACE.get().trigger((ServerPlayerEntity)player);

        if(ray.getLocation().subtract(from).length() > 100)
            CriteriaTriggerInit.SHOOT_PORTAL_FAR.get().trigger((ServerPlayerEntity)player);

        CriteriaTriggerInit.PLACE_PORTALS.get().trigger((ServerPlayerEntity)player);
        player.awardStat(StatsInit.PORTALS_SHOT);
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player),
                new SPortalGunAnimationPacket(getUUID(gun), PortalGunAnimation.SHOOT));
//        PortalPairCache.SERVER.put(getUUID(gun), end, portal);
        level.addFreshEntity(portal);
        PortalManager.put(getUUID(gun), end, portal);

        // todo convert to string
        player.getMainHandItem().getOrCreateTag().putByte("color", (byte)end.ordinal());
    }

//    public static void setPortal(PortalEnd end, @Nullable BlockPos pos) {
//        CompoundNBT nbt = itemStack.getOrCreateTag();
//        UUID uuid;
//
//        if(nbt.contains("gunUUID")) {
//            uuid = nbt.getUUID("gunUUID");
//        } else {
//            uuid = UUID.randomUUID();
//            nbt.putUUID("gunUUID", uuid);
//        }
//
//        return uuid;
//    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !getUUID(newStack).equals(getUUID(oldStack));
    }

    public static int getColorOverride(ItemStack itemStack, World level, LivingEntity entity) {
        CompoundNBT nbt = itemStack.getOrCreateTag();
        if(!nbt.contains("color"))
            return 0;

        byte color = nbt.getByte("color");
        return color == 1 || color == 2 ? 1 : 0;
    }

//    public static int getGrabOverride(ItemStack itemStack, ClientWorld level, LivingEntity entity) {
//        return 0;
//    }
//
//    public static int getAccentOverride(ItemStack itemStack, ClientWorld level, LivingEntity entity) {
//        return 0;
//    }

    // todo maybe change a bit
//    @Override
//    public void appendHoverText(ItemStack stack, World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
//        super.appendHoverText(stack, level, tooltip, flag);
//
//        if(hasPortal(stack, PortalEnd.BLUE, true)) {
//            BlockPos pos = getPortalPosition(stack, PortalEnd.BLUE, true);
//            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "1st portal: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
//        }
//
//        if(hasPortal(stack, PortalEnd.ORANGE, true)) {
//            BlockPos pos = getPortalPosition(stack, PortalEnd.ORANGE, true);
//            tooltip.add(new StringTextComponent(TextFormatting.GOLD + "2nd portal: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
//        }
//    }
    
    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity entity, int i, boolean b) {
        super.inventoryTick(itemStack, level, entity, i, b);

        if(level.isClientSide)
            return;

        UUID uuid = getUUID(itemStack);
        CompoundNBT nbt = itemStack.getOrCreateTag();

        boolean hasBlue = PortalManager.has(uuid, PortalEnd.BLUE);
        boolean hasOrange = PortalManager.has(uuid, PortalEnd.ORANGE);
        if(!nbt.contains("blue") || nbt.getBoolean("blue") != hasBlue)
            nbt.putBoolean("blue", hasBlue);
        if(!nbt.contains("orange") || nbt.getBoolean("orange") != hasOrange)
            nbt.putBoolean("orange", hasOrange);
    }
    
    public static UUID getUUID(ItemStack itemStack) {
        CompoundNBT nbt = itemStack.getOrCreateTag();
        UUID uuid;
        
        if(nbt.contains("gunUUID")) {
            uuid = nbt.getUUID("gunUUID");
        } else {
            uuid = UUID.randomUUID();
            nbt.putUUID("gunUUID", uuid);
        }

        return uuid;
    }
}