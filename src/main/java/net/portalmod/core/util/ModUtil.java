package net.portalmod.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModUtil {

    public static final Style TOOLTIP_STYLE = Style.EMPTY.withColor(TextFormatting.GRAY);

    private static int lastChatNumber = 0;

    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction, int multiplier) {
        Vector3i normal = direction.getNormal();
        return shape.move(normal.getX() * multiplier, normal.getY() * multiplier, normal.getZ() * multiplier);
    }
    
    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction) {
        return moveVoxelShape(shape, direction, 1);
    }
    
    public static BlockRayTraceResult rayTraceBlock(PlayerEntity player, World level, int length) {
        Vector3d rayPath = player.getViewVector(0).scale(length);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
        return level.clip(rayCtx);
    }

    public static List<PortalEntity> getPortalsAlongRay(World level, Vec3 from, Vec3 to, Predicate<PortalEntity> filter) {
        List<PortalEntity> portalChain = new ArrayList<>();
        from = from.clone();
        to = to.clone();

        int limit = 100;
        while(limit-- > 0) {
            AxisAlignedBB rayAABB = new AxisAlignedBB(from.to3d(), to.to3d());

            Vec3 finalFrom = from.clone();
            Optional<PortalEntity> optionalPortal = PortalEntity.getPortals(level, rayAABB, filter)
                    .stream().reduce((o, n) ->
                            n.position().distanceTo(finalFrom.to3d()) < o.position().distanceTo(finalFrom.to3d()) ? n : o);

            if(!optionalPortal.isPresent())
                break;

            PortalEntity portal = optionalPortal.get();
            AxisAlignedBB clipAABB = portal.getBoundingBox().move(new Vec3(portal.getNormal()).mul(-1/16f).to3d());
            boolean traversesPortal = clipAABB.clip(from.to3d(), to.to3d()).isPresent();

            if(!traversesPortal)
                break;

            boolean rightDirection = from.clone().sub(portal.position()).dot(portal.getNormal()) > 0
                    && to.clone().sub(portal.position()).dot(portal.getNormal()) < 0;

            if(!rightDirection)
                break;

            if(!portal.getOtherPortal().isPresent())
                break;

            portalChain.add(portal);

            Mat4 matrix = portal.getSourceBasis().getChangeOfBasisMatrix(portal.getOtherPortal().get().getDestinationBasis());
            from = from.sub(portal.position()).transform(matrix).add(portal.getOtherPortal().get().position());
            to = to.sub(portal.position()).transform(matrix).add(portal.getOtherPortal().get().position());
        }

        return portalChain;
    }

    public static Vector3d getOldPos(Entity entity) {
        return new Vector3d(entity.xo, entity.yo, entity.zo);
    }

    public static void addTooltip(String name, List<ITextComponent> list) {
        if (!PortalModConfigManager.TOOLTIPS.get()) {
            return;
        }

        if (!Screen.hasControlDown()) {
            list.add(new TranslationTextComponent("tooltip.portalmod.hold_control").setStyle(TOOLTIP_STYLE));
            return;
        }

        // Single line
        if (I18n.exists("tooltip.portalmod." + name)) {
            list.add(new TranslationTextComponent("tooltip.portalmod." + name).setStyle(TOOLTIP_STYLE));
            return;
        }

        // Multi line
        int i = 1;
        while (true) {
            String key = "tooltip.portalmod." + name + "_" + i;
            if (!I18n.exists(key)) {
                break;
            }
            list.add(new TranslationTextComponent(key).setStyle(TOOLTIP_STYLE));
            i++;
        }
    }

    public static float symmetricRandom(float width) {
        return new Random().nextFloat() * width * 2 - width;
    }

    public static float symmetricRandom() {
        return symmetricRandom(1);
    }

    public static float randomSoundPitch(float width) {
        return 1 + symmetricRandom(width);
    }

    public static float randomSoundPitch() {
        return randomSoundPitch(0.15f);
    }

    public static float randomSlightSoundPitch() {
        return randomSoundPitch(0.075f);
    }

    public static void sendClientChat(Object... text) {
        ClientWorld clientWorld = Minecraft.getInstance().level;
        if (clientWorld == null) {
            PortalMod.LOGGER.error("Tried to send a client chat message while not in a client environment");
            return;
        }

        // This may not always be accurate
        boolean isClientSide = Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER;

        sendChat(clientWorld, isClientSide, text);
    }

    public static void sendChat(World level, Object... text) {
        sendChat(level, level.isClientSide, text);
    }

    private static void sendChat(World level, boolean isClientSide, Object... text) {
        String formatted = Arrays.stream(text).map(t -> (t == null) ? "null" : t.toString()).collect(Collectors.joining(" "));

        try {
            level.players().forEach(
                    player -> player.displayClientMessage(new StringTextComponent(
                            (isClientSide ? "§3§l[Client " : "§7§l[Server ") + String.format("%03d", lastChatNumber) + "]: §r" + formatted
                    ), false)
            );
        } catch (Exception e) {
            PortalMod.LOGGER.error("Could not send debug chat message", e);
        }

        lastChatNumber = (lastChatNumber + 1) % 1000;
    }

    public static boolean canPlaceAt(BlockItemUseContext context, BlockPos pos) {
        return context.getLevel().getBlockState(pos).canBeReplaced(context)
                && pos.getY() < context.getLevel().getMaxBuildHeight()
                && pos.getY() >= 0;
    }

    public static int getRotationAmount(Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90: return 1;
            case CLOCKWISE_180: return 2;
            case COUNTERCLOCKWISE_90: return 3;
        }
        return 0;
    }
}