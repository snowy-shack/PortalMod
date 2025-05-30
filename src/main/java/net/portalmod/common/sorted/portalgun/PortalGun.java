package net.portalmod.common.sorted.portalgun;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.common.sorted.portal.PortalPair;
import net.portalmod.core.init.*;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PortalGun extends Item {

    public String defaultLeftColor;
    public String defaultRightColor;
    public String defaultAccentColor;

    public PortalGun(Properties properties) {
        this(properties, "blue", "orange", "none");
    }

    // Use for custom portal guns
    public PortalGun(Properties properties, String defaultLeftColor, String defaultRightColor, String defaultAccentColor) {
        super(properties);
        this.defaultLeftColor = defaultLeftColor;
        this.defaultRightColor = defaultRightColor;
        this.defaultAccentColor = defaultAccentColor;
    }

    public static void handleLeftClick() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PortalGunClient::handleLeftClick);
    }
    
    public static void handleRightClick() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PortalGunClient::handleRightClick);
//        PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.SECONDARY).build());
    }

    public static void pickCube(PlayerEntity player, ItemStack gun) {
        // Play lift animation
        World level = player.level;
        if (level instanceof ServerWorld)
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SPortalGunAnimationPacket(getUUID(gun), PortalGunAnimation.LIFT));

        level.playSound(player, player, SoundInit.PORTALGUN_LIFT.get(),
                SoundCategory.PLAYERS, 1, ModUtil.randomSoundPitch());

        if (level instanceof ServerWorld) return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PortalGunGrabSoundClient.handlePacket(player, true));
    }

    public static void dropCube(PlayerEntity player, ItemStack gun) {
        player.level.playSound(player, player,
                SoundInit.PORTALGUN_DROP.get(), SoundCategory.PLAYERS, 1, ModUtil.randomSoundPitch());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PortalGunGrabSoundClient.handlePacket(player, false));

        // Play drop animation
        if (player.level instanceof ServerWorld && gun != null)
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SPortalGunAnimationPacket(getUUID(gun), PortalGunAnimation.DROP));
    }

    private static BlockRayTraceResult customClip(World level, RayTraceContext context) {
        return IBlockReader.traverseBlocks(context, (ctx, pos) -> {
            BlockState blockstate = level.getBlockState(pos);

            if (blockstate.is(BlockTagInit.PORTAL_TRANSPARENT))
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
        if (player.isSpectator()) return;

        gun.getOrCreateTag().putInt("LastPortal", end == PortalEnd.PRIMARY ? -1 : 1);

        // Play shooting animation
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                new SPortalGunAnimationPacket(getUUID(gun), PortalGunAnimation.SHOOT));

        level.playSound(null, player.position().x, player.position().y, player.position().z,
                (Objects.equals(end.getSerializedName(), "primary") ? SoundInit.PORTALGUN_FIRE_PRIMARY.get() : SoundInit.PORTALGUN_FIRE_SECONDARY.get()), SoundCategory.PLAYERS, 1f, ModUtil.randomSoundPitch());

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
            if(dot <= -0.997) {
                CriteriaTriggerInit.SHOOT_MOON.get().trigger((ServerPlayerEntity) player);
            }
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
        boolean isPrimary = end == PortalEnd.PRIMARY;
        if(nbt.contains(isPrimary ? "LeftColor" : "RightColor")) {
            portal.setHue(nbt.getString(isPrimary ? "LeftColor" : "RightColor"));
        }

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
//        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player),
//                new SPortalGunAnimationPacket(getUUID(gun), PortalGunAnimation.SHOOT));
//        level.playSound(null, player.position().x, player.position().y, player.position().z,
//                (Objects.equals(end.getSerializedName(), "primary") ? SoundInit.PORTALGUN_FIRE_PRIMARY.get() : SoundInit.PORTALGUN_FIRE_SECONDARY.get()), SoundCategory.PLAYERS, 1f, 1);
//        PortalPairCache.SERVER.put(getUUID(gun), end, portal);
        level.addFreshEntity(portal);
        PortalManager.put(getUUID(gun), end, portal, level);

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
//        if(hasPortal(stack, PortalEnd.PRIMARY, true)) {
//            BlockPos pos = getPortalPosition(stack, PortalEnd.PRIMARY, true);
//            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "1st portal: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
//        }
//
//        if(hasPortal(stack, PortalEnd.SECONDARY, true)) {
//            BlockPos pos = getPortalPosition(stack, PortalEnd.SECONDARY, true);
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

        boolean hasBlue = PortalManager.has(uuid, PortalEnd.PRIMARY);
        boolean hasOrange = PortalManager.has(uuid, PortalEnd.SECONDARY);
        if(!nbt.contains("primary") || nbt.getBoolean("primary") != hasBlue)
            nbt.putBoolean("primary", hasBlue);
        if(!nbt.contains("secondary") || nbt.getBoolean("secondary") != hasOrange)
            nbt.putBoolean("secondary", hasOrange);

        this.addDefaultNbt(nbt);
    }

    public void addDefaultNbt(CompoundNBT nbt) {
        addDefaultNbt(nbt, this.defaultLeftColor, this.defaultRightColor, this.defaultAccentColor);
    }

    public static void addDefaultNbt(CompoundNBT nbt, String leftColor, String rightColor, String accentColor) {
        if (!nbt.contains("LeftColor"))   nbt.putString("LeftColor", leftColor);
        if (!nbt.contains("RightColor"))  nbt.putString("RightColor", rightColor);
        if (!nbt.contains("LastPortal"))  nbt.putInt("LastPortal", 0);
        if (!nbt.contains("AccentColor")) nbt.putString("AccentColor", accentColor);
        if (!nbt.contains("Locked"))      nbt.putBoolean("Locked", false);
    }

    @Override
    public void fillItemCategory(ItemGroup itemGroup, NonNullList<ItemStack> itemStacks) {
        if (this.allowdedIn(itemGroup)) {
            itemStacks.add(new ItemStack(this));
            itemStacks.add(modifyColors(new ItemStack(this), "light_blue", "blue", "light_blue"));
            itemStacks.add(modifyColors(new ItemStack(this), "yellow", "red", "orange"));
        }
    }

    public static ItemStack modifyColors(ItemStack itemStack, String leftColor, String rightColor, String accentColor) {
        addDefaultNbt(itemStack.getOrCreateTag(), leftColor, rightColor, accentColor);
        return itemStack;
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

    public static Colour getLeftColour(CompoundNBT nbt) {
        DyeColor color = DyeColor.BLUE;
        if (nbt.contains("LeftColor")) {
            color = DyeColor.byName(nbt.getString("LeftColor"), color);
        }
        return new Colour(color.getTextureDiffuseColors());
    }

    public static DyeColor getLeftDyeColour(CompoundNBT nbt) {
        DyeColor color = DyeColor.BLUE;
        if (nbt.contains("LeftColor")) {
            color = DyeColor.byName(nbt.getString("LeftColor"), color);
        }
        return color;
    }

    public static Colour getRightColour(CompoundNBT nbt) {
        DyeColor color = DyeColor.ORANGE;
        if (nbt.contains("RightColor")) {
            color = DyeColor.byName(nbt.getString("RightColor"), color);
        }
        return new Colour(color.getTextureDiffuseColors());
    }

    public static DyeColor getRightDyeColour(CompoundNBT nbt) {
        DyeColor color = DyeColor.ORANGE;
        if (nbt.contains("RightColor")) {
            color = DyeColor.byName(nbt.getString("RightColor"), color);
        }
        return color;
    }

    public static Colour getAccentColour(CompoundNBT nbt) {
        Colour colour = new Colour(1f, 1, 1, 0);
        if (nbt.contains("AccentColor")) {
            String accentColor = nbt.getString("AccentColor");
            if (!accentColor.equals("none")) {
                colour = new Colour(DyeColor.byName(accentColor, DyeColor.RED).getTextureDiffuseColors());
            }
        }
        return colour;
    }

    public static void fizzleGun(World level, PlayerEntity entity) {
        boolean didFizzleAny = false;

        for (ItemStack itemStack : entity.inventory.items) {
            if (itemStack.getItem() instanceof PortalGun) {
                didFizzleAny = fizzleGunItem(itemStack) || didFizzleAny;
            }
        }

        if (didFizzleAny) {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity),
                    new SPortalGunAnimationPacket(UUID.randomUUID(), PortalGunAnimation.FIZZLE));
            level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundInit.PORTALGUN_FIZZLE.get(), SoundCategory.PLAYERS, 1f, 1);
        }
    }

    /**
     * Fizzles one portalgun item.
     * @param itemStack The portalgun item.
     * @return whether any portals got fizzled.
     */
    public static boolean fizzleGunItem(ItemStack itemStack) {
        if (true) return false; // FIXME Temporary disable to prevent crashes
        UUID gunUUID = PortalGun.getUUID(itemStack);
        PortalPair pair = PortalManager.getPair(gunUUID);

        if (pair == null) {
            return false;
        }

        if (pair.has(PortalEnd.PRIMARY)) {
            PortalEntity blue = pair.get(PortalEnd.PRIMARY);
            ((ServerWorld) blue.level).removeEntity(blue, false);
            PortalManager.remove(gunUUID, blue);
        }
        if (pair.has(PortalEnd.SECONDARY)) {
            PortalEntity orange = pair.get(PortalEnd.SECONDARY);
            ((ServerWorld) orange.level).removeEntity(orange, false);
            PortalManager.remove(gunUUID, orange);
        }

        itemStack.getOrCreateTag().putInt("LastPortal", 0);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag iTooltipFlag) {
        String leftColor = getLeftDyeColour(itemStack.getOrCreateTag()).toString();
        String rightColor = getRightDyeColour(itemStack.getOrCreateTag()).toString();

        list.add(new TranslationTextComponent("tooltip.portalmod.portalgun.colors"));
        list.add(new TranslationTextComponent("tooltip.portalmod.colors." + leftColor)
            .append("§7 & ")
            .append(new TranslationTextComponent("tooltip.portalmod.colors." + rightColor)));
        if (Screen.hasControlDown()) list.add(new StringTextComponent(""));

        ModUtil.addTooltip("portalgun", list);
    }
}