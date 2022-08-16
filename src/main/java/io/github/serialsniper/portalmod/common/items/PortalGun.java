package io.github.serialsniper.portalmod.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.common.entities.AbstractCube;
import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import io.github.serialsniper.portalmod.core.enums.PortalGunInteraction;
import io.github.serialsniper.portalmod.core.init.PacketInit;
import io.github.serialsniper.portalmod.core.packet.PortalGunInteractionPacket;
import io.github.serialsniper.portalmod.core.util.PortalPairManager;
import io.github.serialsniper.portalmod.core.util.RayCaster;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class PortalGun extends Item {
    //	public BlockPos orange, blue;
    public PortalEnd lastUsed = PortalEnd.NONE;
    public static HashMap<PortalGun, Pair<PortalableBlock, PortalableBlock>> hierarcy;
    
    public PortalGun(Properties properties) {
        super(properties);
    }
    
    public static void handleLeftClick() {
        if(Minecraft.getInstance().player.hasPassenger(AbstractCube.class)) {
            PortalGun.dropCube(Minecraft.getInstance().player, true);
            PacketInit.INSTANCE.sendToServer(new PortalGunInteractionPacket.Server.Builder(PortalGunInteraction.THROW_CUBE).build());
        } else PacketInit.INSTANCE.sendToServer(new PortalGunInteractionPacket.Server.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.BLUE).build());
    }
    
    public static void handleRightClick() {
        PacketInit.INSTANCE.sendToServer(new PortalGunInteractionPacket.Server.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.ORANGE).build());
    }
    
    public static void dropCube(PlayerEntity player, boolean toBeThrown) {
        List<Entity> cubes = player.getPassengers();
        for(int i = cubes.size() - 1; i >= 0; --i) {
            if(cubes.get(i) instanceof AbstractCube) {
                cubes.get(i).stopRiding();
                if(toBeThrown) {
                    float strength = .2f;
                    cubes.get(i).setDeltaMovement(player.getViewVector(0).multiply(strength, strength, strength));
                }
            }
        }
    }
    
    public static void placePortal(PlayerEntity player, World level, PortalEnd end, ItemStack gun) {
        BlockRayTraceResult ray = RayCaster.castBlock(player, level, 100);

        if(ray.getType() == RayTraceResult.Type.MISS)
            return;

        Direction face = ray.getDirection();
        BlockPos pos = ray.getBlockPos().relative(face);
        PortalEntity portal = new PortalEntity(level);
        
        if(!face.getAxis().isHorizontal()) {
        	Direction upVector = player.getDirection();
        	if(face.getAxisDirection() == AxisDirection.NEGATIVE)
        		upVector = upVector.getOpposite();
    		portal.setUpVector(upVector);
        }
        
        portal.setPos(pos.getX(), pos.getY(), pos.getZ());
        portal.setDirection(face);
        portal.setEnd(end);
        portal.setGunUUID(getUUID(gun));
        if(!portal.adjustShot(ray))
        	return;
        
        level.addFreshEntity(portal);
        player.getMainHandItem().getOrCreateTag().putByte("color", (byte)end.ordinal());
    }
    
    public void placePortalOld(PortalEnd end, World level, PlayerEntity player) {
        if(level.isClientSide())
            return;

        // todo only on portal miss

//		System.out.println((world.getTimeOfDay(0) * 180 + 270) % 360);
//
//		float timeOfDay = Math.abs((world.getDayTime() - 18000) / 12000);
//		System.out.println(timeOfDay);
//		if(timeOfDay < 1) {
//			float angle = (timeOfDay - 1) * 90;
//			System.out.println(angle);
//			System.out.println(player.xRot);
//			if(player.xRot > angle - 3 && player.xRot < angle + 3)
//				System.out.println("lunacy");
//		}

        // todo complete lunacy

        double timeOfDay = (level.getTimeOfDay(0) + .25f);
        double angle = (timeOfDay - (int)timeOfDay) * 2f * Math.PI;
        Vector3d moonVector = new Vector3d(Math.cos(angle), Math.sin(angle), 0);
        double dot = player.getLookAngle().dot(moonVector);
        System.out.println(dot);
        if(dot <= -0.997)
            System.out.println("lunacy");









        int rayLength = 100;
        Vector3d playerRotation = Minecraft.getInstance().player.getViewVector(0);
        Vector3d rayPath = playerRotation.scale(rayLength);

        Vector3d from = Minecraft.getInstance().player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
        BlockRayTraceResult rayHit = level.clip(rayCtx);

        if(rayHit.getType() == RayTraceResult.Type.MISS) {
//			Minecraft.getInstance().player.displayClientMessage(new StringTextComponent("MISS"), false);
            return;
        }

        BlockPos hitLocation = rayHit.getBlockPos();
        Direction side = rayHit.getDirection();
//		Direction horizontalDirection = player.getDirection();

        if(!(level.getBlockState(hitLocation).getBlock() instanceof PortalableBlock))
            return;

        BlockState above = level.getBlockState(hitLocation.above());
        BlockState below = level.getBlockState(hitLocation.below());

        BlockPos baseBlockLocation;

        if(above.getBlock() instanceof PortalableBlock)
            baseBlockLocation = hitLocation;
        else if(below.getBlock() instanceof PortalableBlock)
            baseBlockLocation = hitLocation.below();
        else return;

        ItemStack stack = player.getMainHandItem();

//		Minecraft.getInstance().player.displayClientMessage(new StringTextComponent("Location" + hitLocation.toString()), false);
//		Minecraft.getInstance().player.displayClientMessage(new StringTextComponent("Side" + side.toString()), false);
//		Minecraft.getInstance().player.displayClientMessage(new StringTextComponent("Direction" + horizontalDirection.toString()), false);

        if(hasPortal(stack, end, level))
            ((PortalableBlock)level.getBlockState(baseBlockLocation).getBlock()).fizzlePortal(level, getPortalPosition(stack, end, level));
        BlockPos pos = ((PortalableBlock)level.getBlockState(baseBlockLocation).getBlock()).createPortal(stack, end, side, level, baseBlockLocation, getUUID(stack), rayHit);

        if(pos != null)
//            setPortal(stack, end, pos);

        lastUsed = end;
    }
    
    public static int getColorOverride(ItemStack itemStack, ClientWorld level, LivingEntity entity) {
        if(!itemStack.hasTag())
            return 0;

        CompoundNBT nbt = itemStack.getTag();
        if(!nbt.contains("color"))
            return 0;

        byte color = nbt.getByte("color");
        return color == 1 || color == 2 ? 1 : 0;
    }

    public static int getGrabOverride(ItemStack itemStack, ClientWorld level, LivingEntity entity) {
        return 0;
    }

    public static int getAccentOverride(ItemStack itemStack, ClientWorld level, LivingEntity entity) {
        return 0;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if(hasPortal(stack, PortalEnd.BLUE, true)) {
            BlockPos pos = getPortalPosition(stack, PortalEnd.BLUE, true);
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "1st portal: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }

        if(hasPortal(stack, PortalEnd.ORANGE, true)) {
            BlockPos pos = getPortalPosition(stack, PortalEnd.ORANGE, true);
            tooltip.add(new StringTextComponent(TextFormatting.GOLD + "2nd portal: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        }
    }
    
    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity entity, int i, boolean b) {
    	super.inventoryTick(itemStack, level, entity, i, b);
    	getUUID(itemStack);
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
    
//    public static void removePortal(ItemStack stack, PortalEnd end) {
//        if(!stack.hasTag())
//            return;
//
//        CompoundNBT nbt = stack.getTag();
//        String key = end.getSerializedName();
//
//        if(!nbt.contains(key))
//            return;
//
//        nbt.remove(key);
//        stack.setTag(nbt);
//    }
    
//    public static void setPortal(ItemStack stack, PortalEntity portal, PortalEnd end) {
//        CompoundNBT nbt = new CompoundNBT();
//
//        if(stack.hasTag())
//            nbt = stack.getTag();
//
//        String key = end.getSerializedName();
//        BlockPos blockpos = portal.getPos();
//        nbt.putIntArray(key, new int[] {
//                blockpos.getX(),
//                blockpos.getY(),
//                blockpos.getZ()
//        });
//        nbt.putByte("color", (byte)end.ordinal());
//
//        stack.setTag(nbt);
//    }
    
    public static boolean hasPortal(ItemStack stack, PortalEnd end, World level) {
//        if(!stack.hasTag())
//            return false;
//
//        CompoundNBT nbt = stack.getTag();
//        String key = end.getSerializedName();
//        return nbt.getIntArray(key).length == 3;
    	
    	return hasPortal(stack, end, level.isClientSide);
    }
    
    public static BlockPos getPortalPosition(ItemStack stack, PortalEnd end, World level) {
//        if(!stack.hasTag())
//            return null;
//
//        CompoundNBT nbt = stack.getTag();
//        String key = end.getSerializedName();
//
//        if(!nbt.contains(key))
//            return null;
//
//        int[] pos = nbt.getIntArray(key);
//        return new BlockPos(pos[0], pos[1], pos[2]);
        
    	return getPortalPosition(stack, end, level.isClientSide);
    }
    
    public static boolean hasPortal(ItemStack stack, PortalEnd end, boolean isClientSide) {
//        if(!stack.hasTag())
//            return false;
//
//        CompoundNBT nbt = stack.getTag();
//        String key = end.getSerializedName();
//        return nbt.getIntArray(key).length == 3;
    	
    	UUID gunUUID = getUUID(stack);
    	return PortalPairManager.select(isClientSide).has(gunUUID, end);
    }
    
    public static BlockPos getPortalPosition(ItemStack stack, PortalEnd end, boolean isClientSide) {
        UUID gunUUID = getUUID(stack);
        
        if(!PortalPairManager.select(isClientSide).has(gunUUID, end))
        	return null;
        return PortalPairManager.select(isClientSide).get(gunUUID, end).getPos();
    }
}