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
import net.minecraft.util.Hand;
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
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.init.*;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PortalGun extends Item {

    public String defaultLeftColor;
    public String defaultRightColor;
    public String defaultAccentColor;
    private final PortalGunModel model;

    private static final HashMap<UUID, PortalGun> BY_UUID = new HashMap<>();

    public PortalGun(Properties properties) {
        this(properties, new PortalGunModel(), "blue", "orange", "none");
    }

    // Use for custom portal guns
    public PortalGun(Properties properties, PortalGunModel model, String defaultLeftColor, String defaultRightColor, String defaultAccentColor) {
        super(properties);
        this.defaultLeftColor = defaultLeftColor;
        this.defaultRightColor = defaultRightColor;
        this.defaultAccentColor = defaultAccentColor;
        this.model = model;
    }

    public static void handleLeftClick() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PortalGunClient::handleLeftClick);
    }
    
    public static void handleRightClick() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PortalGunClient::handleRightClick);
//        PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.SECONDARY).build());
    }

    // The portalgun tracks whether it is holding something on its own
    // by using nbt instead of having 100 different things triggering the
    // pick and drop animation all over the place
    public static void updateHolding(ItemStack itemStack, PlayerEntity player) {
        CompoundNBT nbt = itemStack.getOrCreateTag();

        boolean isInMainHand = player.getItemInHand(Hand.MAIN_HAND) == itemStack;
        boolean isInOffHand = player.getItemInHand(Hand.OFF_HAND) == itemStack;

        // Only hold one gun at a time
        boolean isInHand = isInMainHand || isInOffHand && !(player.getMainHandItem().getItem() instanceof PortalGun);

        boolean wasHolding = nbt.contains("Holding") && nbt.getBoolean("Holding");
        boolean isHolding = isInHand && player.getPassengers().stream().anyMatch(entity -> entity instanceof TestElementEntity);

        if (isHolding && !wasHolding) {
            pickCube(player, itemStack);
        }

        if (!isHolding && wasHolding) {
            dropCube(player, itemStack);
        }
    }

    public static void setHolding(ItemStack itemStack, boolean holding) {
        CompoundNBT nbt = itemStack.getOrCreateTag();
        nbt.putBoolean("Holding", holding);
    }

    public static void pickCube(PlayerEntity player, ItemStack gun) {
        setHolding(gun, true);

        player.level.playSound(player, player, SoundInit.PORTALGUN_LIFT.get(),
                SoundCategory.PLAYERS, 1, ModUtil.randomSoundPitch());

        // Play lift animation
        Optional<UUID> uuid = getUUID(gun);
        if (player.level instanceof ServerWorld && uuid.isPresent()) {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SPortalGunAnimationPacket(uuid.get(), PortalGunAnimation.LIFT));
            return;
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PortalGunGrabSoundClient.handlePacket(player, true));
    }

    public static void dropCube(PlayerEntity player, ItemStack gun) {
        setHolding(gun, false);

        player.level.playSound(player, player,
                SoundInit.PORTALGUN_DROP.get(), SoundCategory.PLAYERS, 1, ModUtil.randomSoundPitch());

        // Play drop animation
        Optional<UUID> uuid = getUUID(gun);
        if (player.level instanceof ServerWorld && uuid.isPresent()) {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SPortalGunAnimationPacket(uuid.get(), PortalGunAnimation.DROP));
            return;
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PortalGunGrabSoundClient.handlePacket(player, false));
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        dropCube(player, item);

        return super.onDroppedByPlayer(item, player);
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

    private static void triggerMoonAdvancement(World level, ServerPlayerEntity player) {
        double timeOfDay = level.getTimeOfDay(0) + .25f;
        double angle = (timeOfDay - (int)timeOfDay) * 2f * Math.PI;
        Vector3d moonVector = new Vector3d(Math.cos(angle), Math.sin(angle), 0);
        if(player.getLookAngle().dot(moonVector) <= -0.997)
            CriteriaTriggerInit.SHOOT_MOON.get().trigger(player);
    }

    private static void triggerPortalAdvancements(World level, ServerPlayerEntity player, PortalEntity portal, double distance) {
        boolean allQualityBlocks = portal.getBlocksBehind().stream().allMatch(pos ->
                level.getBlockState(pos).is(BlockTagInit.PORTALABLE_QUALITY));

        CriteriaTriggerInit.PLACE_PORTALS.get().trigger(player);

        if(allQualityBlocks)
            CriteriaTriggerInit.PORTAL_SURFACE.get().trigger(player);

        if(distance > 100)
            CriteriaTriggerInit.SHOOT_PORTAL_FAR.get().trigger(player);

        player.awardStat(StatsInit.PORTALS_SHOT);
    }

    public static void placePortal(PlayerEntity player, World level, PortalEnd end, ItemStack gun) {
        if(player.isSpectator() || level.isClientSide)
            return;

        Optional<UUID> uuid = getUUID(gun);
        if (!uuid.isPresent()) return;

        // Play shooting animation and sound
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                new SPortalGunAnimationPacket(uuid.get(), PortalGunAnimation.SHOOT));

        level.playSound(null, player.position().x, player.position().y, player.position().z,
                (Objects.equals(end.getSerializedName(), "primary") ? SoundInit.PORTALGUN_FIRE_PRIMARY.get() : SoundInit.PORTALGUN_FIRE_SECONDARY.get()), SoundCategory.PLAYERS, 1f, ModUtil.randomSoundPitch());

        Vector3d rayPath = player.getViewVector(0).scale(200);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);
        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);
        BlockRayTraceResult ray = customClip(level, rayCtx);

        if(ray.getType() == RayTraceResult.Type.MISS) {
            triggerMoonAdvancement(level, (ServerPlayerEntity)player);
            return;
        }

        Vec3 position = new Vec3(ray.getLocation());
        Direction face = ray.getDirection();
        Direction up = face.getAxis().isHorizontal() ? Direction.UP : player.getDirection();

        CompoundNBT nbt = gun.getOrCreateTag();
        boolean isPrimary = end == PortalEnd.PRIMARY;
        String hue = "blue";
        if(nbt.contains(isPrimary ? "LeftColor" : "RightColor")) {
            hue = nbt.getString(isPrimary ? "LeftColor" : "RightColor");
        }

        boolean inFizzler = AABBUtil.getBlocksWithin(player.getBoundingBox()).stream()
                .map(pos -> level.getBlockState(pos).getBlock())
                .anyMatch(block -> block == BlockInit.FIZZLER_EMITTER.get() || block == BlockInit.FIZZLER_FIELD.get());

        PortalEntity portal = null;

        if(!inFizzler) {
            portal = PortalPlacer.placePortal(level, end, hue, uuid.get(), position.clone(), face, up);
        }

        if(portal == null)
            return;

        triggerPortalAdvancements(level, (ServerPlayerEntity)player, portal, ray.getLocation().subtract(from).length());

        // todo convert to string
        gun.getOrCreateTag().putInt("LastPortal", end == PortalEnd.PRIMARY ? -1 : 1);
        player.getMainHandItem().getOrCreateTag().putByte("color", (byte)end.ordinal());
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        Optional<UUID> newUUID = getUUID(newStack);
        Optional<UUID> oldUUID = getUUID(oldStack);
        return slotChanged || oldUUID.isPresent() && newUUID.isPresent() && !oldUUID.equals(newUUID);
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

        if (entity instanceof PlayerEntity) {
            updateHolding(itemStack, (PlayerEntity) entity);
        }

        if(level.isClientSide)
            return;

        addUUID(itemStack);

        Optional<UUID> uuid = getUUID(itemStack);
        CompoundNBT nbt = itemStack.getOrCreateTag();

        boolean hasBlue = uuid.isPresent() && PortalManager.getInstance().has(uuid.get(), PortalEnd.PRIMARY);
        boolean hasOrange = uuid.isPresent() && PortalManager.getInstance().has(uuid.get(), PortalEnd.SECONDARY);
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

    public static void addUUID(ItemStack itemStack) {
        CompoundNBT nbt = itemStack.getOrCreateTag();

        if (!nbt.contains("gunUUID")) {
            nbt.putUUID("gunUUID", UUID.randomUUID());
        }
    }

    public static void removeUUID(ItemStack itemStack) {
        CompoundNBT nbt = itemStack.getOrCreateTag();
        nbt.remove("gunUUID");
    }

    public static Optional<UUID> getUUID(ItemStack itemStack) {
        CompoundNBT nbt = itemStack.getOrCreateTag();

        // Return optional to make it clear that the UUID is not always present
        if (nbt.contains("gunUUID") && itemStack.getItem() instanceof PortalGun) {
            UUID uuid = nbt.getUUID("gunUUID");
            BY_UUID.put(uuid, (PortalGun) itemStack.getItem());
            return Optional.of(uuid);
        }

        return Optional.empty();
    }

    public static void onDuplicate(ItemStack itemStack) {
        removeUUID(itemStack);
        addUUID(itemStack);
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

    public PortalGunModel getModel() {
        return this.model;
    }

    public static PortalGunModel getModel(UUID gunUUID) {
        return BY_UUID.get(gunUUID).getModel();
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
     * @return whether any portals got fizzled.
     */
    public static boolean fizzleGunItem(ItemStack itemStack) {
        Optional<UUID> gunUUID = PortalGun.getUUID(itemStack);
        if (!gunUUID.isPresent()) return false;

        PortalPair pair = PortalManager.getInstance().getPair(gunUUID.get());
        if (pair == null) return false;

        if (pair.has(PortalEnd.PRIMARY)) {
            PortalEntity primary = pair.get(PortalEnd.PRIMARY);
            primary.scheduleRemoval();
        }
        if (pair.has(PortalEnd.SECONDARY)) {
            PortalEntity secondary = pair.get(PortalEnd.SECONDARY);
            secondary.scheduleRemoval();
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