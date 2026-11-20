package net.portalmod.common.sorted.portalgun;

import net.portalmod.core.interfaces.CreativeTabFiller;
import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketDistributor;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.fizzler.FizzlerEmitterBlock;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.*;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class PortalGun extends Item implements CreativeTabFiller {

    public String defaultLeftColor;
    public String defaultRightColor;
    public String defaultAccentColor;

    private static final HashMap<UUID, PortalGun> BY_UUID = new HashMap<>();

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

    // The portalgun tracks whether it is holding something on its own
    // by using nbt instead of having 100 different things triggering the
    // pick and drop animation all over the place
    public static void updateHolding(ItemStack itemStack, Player player) {
        if(!itemStack.hasTag())
            return;

        CompoundTag nbt = itemStack.getTag();

        boolean isInMainHand = player.getItemInHand(InteractionHand.MAIN_HAND) == itemStack;
        boolean isInOffHand = player.getItemInHand(InteractionHand.OFF_HAND) == itemStack;

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
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.putBoolean("Holding", holding);
    }

    public static void pickCube(Player player, ItemStack gun) {
        setHolding(gun, true);

        player.level().playSound(player, player, SoundInit.PORTALGUN_LIFT.get(),
                SoundSource.PLAYERS, 1, ModUtil.randomSoundPitch());

        // Play lift animation
        Optional<UUID> uuid = getUUID(gun);
        if (player.level() instanceof ServerLevel && uuid.isPresent()) {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SPortalGunAnimationPacket(uuid.get(), PortalGunAnimation.LIFT));
            return;
        }

        ClientOnly.run(() -> () ->
                PortalGunGrabSoundClient.handlePacket(player, true));
    }

    public static void dropCube(Player player, ItemStack gun) {
        setHolding(gun, false);

        player.level().playSound(player, player,
                SoundInit.PORTALGUN_DROP.get(), SoundSource.PLAYERS, 1, ModUtil.randomSoundPitch());

        // Play drop animation
        Optional<UUID> uuid = getUUID(gun);
        if (player.level() instanceof ServerLevel && uuid.isPresent()) {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SPortalGunAnimationPacket(uuid.get(), PortalGunAnimation.DROP));
            return;
        }

        ClientOnly.run(() -> () ->
                PortalGunGrabSoundClient.handlePacket(player, false));
    }

    /** Called by ServerPlayerHooksMixin when the player drops this item (Forge: Item#onDroppedByPlayer). */
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        dropCube(player, item);

        return true;
    }

    public static BlockHitResult customClip(Level level, ClipContext context) {
        return BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (ctx, pos) -> {
            BlockState blockstate = level.getBlockState(pos);

            if (blockstate.is(BlockTagInit.PORTAL_TRANSPARENT))
                return null;
            FluidState fluidstate = level.getFluidState(pos);

            net.minecraft.world.phys.Vec3 vector3d = ctx.getFrom();
            net.minecraft.world.phys.Vec3 vector3d1 = ctx.getTo();
            VoxelShape voxelshape = blockstate.getBlock().getShape(blockstate, level, pos, CollisionContext.empty());

            Block block = blockstate.getBlock();
            if(block == BlockInit.FIZZLER_EMITTER.get()) {
                if(blockstate.getValue(FizzlerEmitterBlock.ACTIVE)) {
                    voxelshape = ((FizzlerEmitterBlock)block).getFieldShape(blockstate);
                }
            } else if(block == BlockInit.FIZZLER_FIELD.get()) {
                voxelshape = ((FizzlerFieldBlock)block).getFieldShape(blockstate);
            }

            BlockHitResult blockraytraceresult = level.clipWithInteractionOverride(vector3d, vector3d1, pos, voxelshape, blockstate);
            VoxelShape voxelshape1 = ctx.getFluidShape(fluidstate, level, pos);

            BlockHitResult blockraytraceresult1 = voxelshape1.clip(vector3d, vector3d1, pos);
            double d0 = blockraytraceresult == null ? Double.MAX_VALUE : ctx.getFrom().distanceToSqr(blockraytraceresult.getLocation());
            double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : ctx.getFrom().distanceToSqr(blockraytraceresult1.getLocation());
            return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
        }, ctx -> {
            net.minecraft.world.phys.Vec3 vector3d = ctx.getFrom().subtract(ctx.getTo());
            return BlockHitResult.miss(ctx.getTo(), Direction.getNearest(vector3d.x, vector3d.y, vector3d.z), new BlockPos(ctx.getTo()));
        });
    }

    private static void triggerMoonAdvancement(Level level, ServerPlayer player) {
        double timeOfDay = level.getTimeOfDay(0) + .25f;
        double angle = (timeOfDay - (int)timeOfDay) * 2f * Math.PI;
        net.minecraft.world.phys.Vec3 moonVector = new net.minecraft.world.phys.Vec3(Math.cos(angle), Math.sin(angle), 0);
        boolean lookingUp = player.getLookAngle().dot(new net.minecraft.world.phys.Vec3(0, 1, 0)) > 0;
        boolean lookingAtTheMoon = player.getLookAngle().dot(moonVector) <= -0.997;

        if(lookingUp && lookingAtTheMoon) {
            CriteriaTriggerInit.SHOOT_MOON.get().trigger(player);
        }
    }

    private static void triggerPortalAdvancements(Level level, ServerPlayer player, PortalEntity portal, double distance) {
        boolean allQualityBlocks = portal.getBlocksBehind().stream().allMatch(pos ->
                level.getBlockState(pos).is(BlockTagInit.PORTALABLE_QUALITY));

        CriteriaTriggerInit.PLACE_PORTALS.get().trigger(player);

        if(allQualityBlocks)
            CriteriaTriggerInit.PORTAL_SURFACE.get().trigger(player);

        if(distance > 100)
            CriteriaTriggerInit.SHOOT_PORTAL_FAR.get().trigger(player);

        player.awardStat(StatsInit.PORTALS_SHOT);
    }

    public static void placePortal(Player player, Level level, PortalEnd end, ItemStack gun, BlockHitResult ray) {
        if(player.isSpectator() || level.isClientSide)
            return;

        Optional<UUID> uuid = getUUID(gun);
        if (!uuid.isPresent()) return;

        CompoundTag nbt = gun.getOrCreateTag();

        boolean isPrimary = end == PortalEnd.PRIMARY;

        if (nbt.contains("Locked") && nbt.getString("Locked").equals(isPrimary ? "Left" : "Right")) {
            return;
        }

        // Play shooting animation and sound
        PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SPortalGunAnimationPacket(uuid.get(), PortalGunAnimation.SHOOT));

        level.playSound(null, player.position().x, player.position().y, player.position().z,
                (Objects.equals(end.getSerializedName(), "primary") ? SoundInit.PORTALGUN_FIRE_PRIMARY.get() : SoundInit.PORTALGUN_FIRE_SECONDARY.get()), SoundSource.PLAYERS, 1f, ModUtil.randomSoundPitch());

        if(ray.getType() == HitResult.Type.MISS) {
            triggerMoonAdvancement(level, (ServerPlayer)player);
            return;
        }

        Vec3 position = new Vec3(ray.getLocation());
        Direction face = ray.getDirection();
        Direction up = face.getAxis().isHorizontal() ? Direction.UP : player.getDirection();

        String hue;
        if(nbt.contains(isPrimary ? "LeftColor" : "RightColor")) {
            hue = nbt.getString(isPrimary ? "LeftColor" : "RightColor");
        } else {
            hue = "blue";
        }

        boolean inFizzler = AABBUtil.getBlocksWithin(player.getBoundingBox()).stream()
                .anyMatch(pos -> {
                    BlockState state = level.getBlockState(pos);
                    VoxelShape voxelshape;

                    if(state.getBlock() == BlockInit.FIZZLER_EMITTER.get()) {
                        voxelshape = ((FizzlerEmitterBlock)BlockInit.FIZZLER_EMITTER.get()).getFieldShape(state);
                    } else if(state.getBlock() == BlockInit.FIZZLER_FIELD.get()) {
                        voxelshape = ((FizzlerFieldBlock)BlockInit.FIZZLER_FIELD.get()).getFieldShape(state);
                    } else {
                        return false;
                    }

                    VoxelShape movedBlockShape = voxelshape.move(pos.getX(), pos.getY(), pos.getZ());
                    VoxelShape entityShape = Shapes.create(player.getBoundingBox());
                    return Shapes.joinIsNotEmpty(movedBlockShape, entityShape, BooleanOp.AND);
                });

        PortalEntity portal;
        double distance = ray.getLocation().subtract(player.getEyePosition(0)).length();
        int ticks = (int)Math.ceil(distance / 2);

        PortalEnd finalEnd = end;
        Consumer<PortalEntity> onPlace = placedPortal -> {
            if(placedPortal == null) {
                level.playSound(null, position.x, position.y, position.z, SoundInit.PORTALGUN_MISS.get(), SoundSource.PLAYERS, 1f, ModUtil.randomSlightSoundPitch());
                PacketInit.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension),
                        new SPortalGunFailShotPacket(position, new Vec3(face), new Vec3(up), hue));
                return;
            }

            triggerPortalAdvancements(level, (ServerPlayer)player, placedPortal, distance);

            // todo convert to string
            gun.getOrCreateTag().putInt("LastPortal", finalEnd == PortalEnd.PRIMARY ? -1 : 1);
            player.getMainHandItem().getOrCreateTag().putByte("color", (byte) finalEnd.ordinal());
        };

        if(!inFizzler) {
            // Player shots never use the autoportal-style `override` (which would also kill
            // your own other-coloured portal during a self-bump). Instead the gamerule drives
            // overwriteForeignPortals, which only evicts portals that belong to a different
            // gun; same-gun portals keep bumping as before.
            boolean overwriteForeign = level.getGameRules().getBoolean(GameRuleInit.ALLOW_PORTAL_OVERWRITE);
            if(level.getGameRules().getBoolean(GameRuleInit.PORTAL_SLOWSHOT)) {
                PortalManager.getInstance().schedulePlacement(level, end, hue, uuid.get(), position.clone(), face, up, false, overwriteForeign, Direction.orderedByNearest(player), (ServerPlayer) player, ticks, onPlace);
            } else {
                portal = PortalPlacer.placePortal(level, end, hue, uuid.get(), position.clone(), face, up, false, overwriteForeign, Direction.orderedByNearest(player), (ServerPlayer) player);
                onPlace.accept(portal);
            }
        } else {
            level.playSound(null, position.x, position.y, position.z, SoundInit.PORTALGUN_MISS.get(), SoundSource.PLAYERS, 1f, ModUtil.randomSlightSoundPitch());
            PacketInit.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension),
                    new SPortalGunFailShotPacket(position, new Vec3(face), new Vec3(up), hue));
        }
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        boolean slotChanged = false; // Fabric only asks about NBT changes on the same slot
        Optional<UUID> newUUID = getUUID(newStack);
        Optional<UUID> oldUUID = getUUID(oldStack);
        return slotChanged || oldUUID.isPresent() && newUUID.isPresent() && !oldUUID.equals(newUUID);
    }

    public static int getColorOverride(ItemStack itemStack, Level level, LivingEntity entity) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        if(!nbt.contains("color"))
            return 0;

        byte color = nbt.getByte("color");
        return color == 1 || color == 2 ? 1 : 0;
    }
    
    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean b) {
        super.inventoryTick(itemStack, level, entity, i, b);

        if (entity instanceof Player) {
            updateHolding(itemStack, (Player) entity);
        }

        if(level.isClientSide)
            return;

        addUUID(itemStack);

        Optional<UUID> uuid = getUUID(itemStack);
        CompoundTag nbt = itemStack.getOrCreateTag();

        boolean hasBlue = uuid.isPresent() && PortalManager.getInstance().has(uuid.get(), PortalEnd.PRIMARY);
        boolean hasOrange = uuid.isPresent() && PortalManager.getInstance().has(uuid.get(), PortalEnd.SECONDARY);
        if(!nbt.contains("primary") || nbt.getBoolean("primary") != hasBlue)
            nbt.putBoolean("primary", hasBlue);
        if(!nbt.contains("secondary") || nbt.getBoolean("secondary") != hasOrange)
            nbt.putBoolean("secondary", hasOrange);

        this.addDefaultNbt(nbt);
    }

    public void addDefaultNbt(CompoundTag nbt) {
        addDefaultNbt(nbt, this.defaultLeftColor, this.defaultRightColor, this.defaultAccentColor);
    }

    public static void addDefaultNbt(CompoundTag nbt, String leftColor, String rightColor, String accentColor) {
        if (!nbt.contains("LeftColor"))   nbt.putString("LeftColor", leftColor);
        if (!nbt.contains("RightColor"))  nbt.putString("RightColor", rightColor);
        if (!nbt.contains("LastPortal"))  nbt.putInt("LastPortal", 0);
        if (!nbt.contains("AccentColor")) nbt.putString("AccentColor", accentColor);
        if (!nbt.contains("Locked"))      nbt.putString("Locked", "None");
    }

    public void fillCreativeTab(java.util.List<ItemStack> itemStacks) {
        if (true) {
            itemStacks.add(new ItemStack(this));
            itemStacks.add(modifyColors(new ItemStack(this), "light_blue", "purple", "light_blue"));
            itemStacks.add(modifyColors(new ItemStack(this), "yellow", "red", "orange"));
        }
    }

    public static ItemStack modifyColors(ItemStack itemStack, String leftColor, String rightColor, String accentColor) {
        addDefaultNbt(itemStack.getOrCreateTag(), leftColor, rightColor, accentColor);
        return itemStack;
    }

    public static void addUUID(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrCreateTag();

        if (!nbt.contains("gunUUID")) {
            nbt.putUUID("gunUUID", UUID.randomUUID());
        }
    }

    public static void removeUUID(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.remove("gunUUID");
    }

    public static Optional<UUID> getUUID(ItemStack itemStack) {
        if(!itemStack.hasTag())
            return Optional.empty();

        CompoundTag nbt = itemStack.getTag();
        if(nbt == null)
            return Optional.empty();

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

    public static Colour getLeftColour(CompoundTag nbt) {
        return PortalColors.getColour(getLeftDyeColour(nbt).getName());
    }

    public static DyeColor getLeftDyeColour(CompoundTag nbt) {
        DyeColor color = DyeColor.BLUE; // Fallback

        if (nbt.getString("Locked").equals("Left")) return getRightDyeColour(nbt);

        if (nbt.contains("LeftColor")) {
            try {
                color = DyeColor.byName(nbt.getString("LeftColor"), color);
            } catch(NullPointerException ignored) {}
        }

        return color;
    }

    public static Colour getRightColour(CompoundTag nbt) {
        return PortalColors.getColour(getRightDyeColour(nbt).getName());
    }

    public static DyeColor getRightDyeColour(CompoundTag nbt) {
        DyeColor color = DyeColor.ORANGE; // Fallback

        if (nbt.getString("Locked").equals("Right")) return getLeftDyeColour(nbt);

        if (nbt.contains("RightColor")) {
            try {
                color = DyeColor.byName(nbt.getString("RightColor"), color);
            } catch(NullPointerException ignored) {}
        }

        return color;
    }

    public static PortalGunModel getModel(UUID gunUUID) {
        return PortalGunModelManager.getInstance().getModel(gunUUID);
    }

    public static Colour getAccentColour(CompoundTag nbt) {
        Colour colour = new Colour(1f, 1, 1, 0);
        if (nbt.contains("AccentColor")) {
            String accentColor = nbt.getString("AccentColor");
            if (!accentColor.equals("none")) {
                colour = new Colour(PortalColors.getColor(accentColor).getRGB());
            }
        }
        return colour;
    }

    public static void fizzleGunsInInventory(Player player) {
        if (player.level().isClientSide) {
            // The server does not retain a player's old position or velocity, so it can miss
            // fast/oblique fizzler traversal where neither the old nor the new bounding box
            // intersects the fizzler but the swept segment between them does. The local
            // client has the full movement data and is the one that reliably catches this,
            // so it asks the server to fizzle on its behalf. The server handler uses
            // getSender(), so only the sending player's own guns are affected -- and the
            // PlayerEntityMixin guard makes sure only the LOCAL player sends it.
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.FIZZLE).build());
            return;
        }

        boolean didFizzleAny = false;

        ArrayList<ItemStack> test = new ArrayList<>(player.getInventory().items);
        test.add(player.getOffhandItem());

        for (ItemStack itemStack : test) {
            if (itemStack.getItem() instanceof PortalGun) {
                didFizzleAny = fizzleGunItem(itemStack) || didFizzleAny;
            }
        }

        if (didFizzleAny) {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SPortalGunAnimationPacket(UUID.randomUUID(), PortalGunAnimation.FIZZLE));
            player.level().playSound(null, player.position().x, player.position().y, player.position().z, SoundInit.PORTALGUN_FIZZLE.get(), SoundSource.PLAYERS, 1f, ModUtil.randomSlightSoundPitch());
        }
    }

    /**
     * Fizzles one portalgun item.
     * @return whether any portals got fizzled.
     */
    public static boolean fizzleGunItem(ItemStack itemStack) {
        Optional<UUID> gunUUID = PortalGun.getUUID(itemStack);
        if (!gunUUID.isPresent()) return false;

        PortalManager.getInstance().clearScheduledPlacements(gunUUID.get());

        PortalPair pair = PortalManager.getInstance().getPair(gunUUID.get());
        if (pair == null) return false;
        boolean fizzledAPortal = false;

        String lock = "";
        if (itemStack.hasTag()) {
            itemStack.getTag().contains("Locked");
            lock = itemStack.getTag().getString("Locked");
        }

        if (pair.has(PortalEnd.PRIMARY) && !lock.equals("Left")) {
            PortalEntity primary = pair.get(PortalEnd.PRIMARY);
            PortalManager.getInstance().scheduleRemoval(primary);
            fizzledAPortal = true;
        }
        if (pair.has(PortalEnd.SECONDARY) && !lock.equals("Right")) {
            PortalEntity secondary = pair.get(PortalEnd.SECONDARY);
            PortalManager.getInstance().scheduleRemoval(secondary);
            fizzledAPortal = true;
        }

        itemStack.getOrCreateTag().putInt("LastPortal", 0);
        return fizzledAPortal;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag iTooltipFlag) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        String lock = nbt.contains("Locked") ? nbt.getString("Locked") : "None";

        String leftColorName = lock.equals("Left") ? "locked" : getLeftDyeColour(nbt).toString();
        String rightColorName = lock.equals("Right") ? "locked" : getRightDyeColour(nbt).toString();

        int leftColor = lock.equals("Left") ? ModUtil.dyeColorValue(DyeColor.LIGHT_GRAY) : PortalColors.getColor(leftColorName).getRGB();
        int rightColor = lock.equals("Right") ? ModUtil.dyeColorValue(DyeColor.LIGHT_GRAY) : PortalColors.getColor(rightColorName).getRGB();

        list.add(ModUtil.tooltipComponent("tooltip.portalmod.portalgun.colors"));
        list.add(Component.translatable("tooltip.portalmod.colors." + leftColorName).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(leftColor)))
                .append("§7 & ")
                .append(Component.translatable("tooltip.portalmod.colors." + rightColorName).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rightColor))))
        );
        if (Screen.hasControlDown()) list.add(Component.literal(""));

        ModUtil.addTooltip("portalgun", list);
    }

    @Override
    public Component getName(ItemStack item) {
        CompoundTag nbt = item.getOrCreateTag();
        Style colorStyle = Style.EMPTY.withColor(TextColor.fromRgb( getAccentColour(nbt).getRGBValue() ));

        return super.getName(item).copy().setStyle(colorStyle);
    }
}