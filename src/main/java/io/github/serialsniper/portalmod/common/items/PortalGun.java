package io.github.serialsniper.portalmod.common.items;

import java.util.*;

import com.mojang.datafixers.util.*;

import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.util.*;
import net.minecraft.client.world.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;

public class PortalGun extends Item {
//	public BlockPos orange, blue;
	public PortalEnd lastUsed = PortalEnd.NONE;
	public static HashMap<PortalGun, Pair<PortalableBlock, PortalableBlock>> hierarcy;
	
	public PortalGun(Properties properties) {
		super(properties);
//		properties.setISTER(() -> (Callable<ItemStackTileEntityRenderer>) new PortalGunISTER());
//		Portal2.LOGGER.debug("ISTER ADDED");
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(handIn != Hand.MAIN_HAND)
			return ActionResult.fail(playerIn.getItemInHand(handIn));

		playerIn.getCooldowns().addCooldown(this, 10);
		placePortal(PortalEnd.ORANGE, worldIn, playerIn);
		return ActionResult.success(playerIn.getItemInHand(handIn));
	}

	public void placePortal(PortalEnd end, World world, PlayerEntity player) {
		if(world.isClientSide())
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








		
		Double rayLength = 100d;
		Vector3d playerRotation = Minecraft.getInstance().player.getViewVector(0);
		Vector3d rayPath = playerRotation.scale(rayLength);
		
		Vector3d from = Minecraft.getInstance().player.getEyePosition(0);
		Vector3d to = from.add(rayPath);
		
		RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
		BlockRayTraceResult rayHit = world.clip(rayCtx);
		
		if(rayHit.getType() == RayTraceResult.Type.MISS) {
//			Minecraft.getInstance().player.displayClientMessage(new StringTextComponent("MISS"), false);
			return;
		}
		
		BlockPos hitLocation = rayHit.getBlockPos();
		Direction side = rayHit.getDirection();
//		Direction horizontalDirection = player.getDirection();
		
		if(!(world.getBlockState(hitLocation).getBlock() instanceof PortalableBlock))
			return;
		
		BlockState above = world.getBlockState(hitLocation.above());
		BlockState below = world.getBlockState(hitLocation.below());
		
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
		
		if(hasPortal(stack, end))
			((PortalableBlock)world.getBlockState(baseBlockLocation).getBlock()).fizzlePortal(world, getPortalPosition(stack, end));
		BlockPos pos = ((PortalableBlock)world.getBlockState(baseBlockLocation).getBlock()).createPortal(stack, end, side, world, baseBlockLocation, getUUID(stack), rayHit);
		
		if(pos != null)
			setPortalNBT(stack, end, pos);
		
		lastUsed = end;
	}
	
	public static int getModelOverride(ItemStack stack, ClientWorld world, LivingEntity entity) {
		if(!(entity instanceof PlayerEntity) || !(entity.getMainHandItem().getItem() instanceof PortalGun))
			return 0;
		
		PortalEnd lastUsed = ((PortalGun)entity.getMainHandItem().getItem()).lastUsed;
		return lastUsed == PortalEnd.NONE ? 0 : (lastUsed == PortalEnd.BLUE ? 1 : 2);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);
		
		if(hasPortal(stack, PortalEnd.BLUE)) {
			BlockPos pos = getPortalPosition(stack, PortalEnd.BLUE);
			tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Blue: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
		}
		if(hasPortal(stack, PortalEnd.ORANGE)) {
			BlockPos pos = getPortalPosition(stack, PortalEnd.ORANGE);
			tooltip.add(new StringTextComponent(TextFormatting.GOLD + "Orange: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
		}
	}
	
	public static UUID getUUID(ItemStack stack) {
		CompoundNBT nbt = new CompoundNBT();
		
		if(stack.hasTag()) {
			nbt = stack.getTag();

			if(nbt.contains("uuid"))
				return nbt.getUUID("uuid");
		}
		
		UUID uuid = UUID.randomUUID();
		
		nbt.putUUID("uuid", uuid);
		stack.setTag(nbt);
		
		return uuid;
	}
	
	public static void deletePortal(ItemStack stack, PortalEnd end) {
		if(!stack.hasTag())
			return;
		
		CompoundNBT nbt = stack.getTag();
		
		String end_str = end.getSerializedName();
		
		nbt.putBoolean(end_str, false);
		nbt.remove(end_str + "_x");
		nbt.remove(end_str + "_y");
		nbt.remove(end_str + "_z");
		
		stack.setTag(nbt);
	}
	
	public static void setPortalNBT(ItemStack stack, PortalEnd end, BlockPos baseBlockLocation) {
		CompoundNBT nbt = new CompoundNBT();
		
		if(stack.hasTag())
			nbt = stack.getTag();

		String end_str = end.getSerializedName();
		
		nbt.putBoolean(end_str, true);
		nbt.putInt(end_str + "_x", baseBlockLocation.getX());
		nbt.putInt(end_str + "_y", baseBlockLocation.getY());
		nbt.putInt(end_str + "_z", baseBlockLocation.getZ());

		stack.setTag(nbt);
	}
	
	public static boolean hasPortal(ItemStack stack, PortalEnd end) {
		if(!stack.hasTag())
			return false;
		
		CompoundNBT nbt = stack.getTag();
		String end_str = end.getSerializedName();
		
		return nbt.getBoolean(end_str);
	}
	
	public static BlockPos getPortalPosition(ItemStack stack, PortalEnd end) {
		if(!stack.hasTag())
			return null;
		
		CompoundNBT nbt = stack.getTag();
		String end_str = end.getSerializedName();
		
		int x = nbt.getInt(end_str + "_x");
		int y = nbt.getInt(end_str + "_y");
		int z = nbt.getInt(end_str + "_z");
		return new BlockPos(x, y, z);
	}
}