package net.portalmod.core.util;

import com.mojang.datafixers.util.Pair;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.Util;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class ModUtil {

    public static final Style TOOLTIP_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);

    private static int lastChatNumber = 0;

    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction, int multiplier) {
        Vec3i normal = direction.getNormal();
        return shape.move(normal.getX() * multiplier, normal.getY() * multiplier, normal.getZ() * multiplier);
    }
    
    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction) {
        return moveVoxelShape(shape, direction, 1);
    }
    
    public static BlockHitResult rayTraceBlock(Player player, Level level, int length) {
        net.minecraft.world.phys.Vec3 rayPath = player.getViewVector(0).scale(length);
        net.minecraft.world.phys.Vec3 from = player.getEyePosition(0);
        net.minecraft.world.phys.Vec3 to = from.add(rayPath);

        ClipContext rayCtx = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null);
        return level.clip(rayCtx);
    }

    public static BlockHitResult customClip(Level level, ClipContext context, Function<BlockPos, Optional<VoxelShape>> shapeOverride) {
        return BlockGetter.traverseBlocks(context, (ctx, pos) -> {
            BlockState blockstate = level.getBlockState(pos);
            FluidState fluidstate = level.getFluidState(pos);

            net.minecraft.world.phys.Vec3 vector3d = ctx.getFrom();
            net.minecraft.world.phys.Vec3 vector3d1 = ctx.getTo();
            VoxelShape voxelshape = shapeOverride.apply(pos).orElse(ctx.getBlockShape(blockstate, level, pos));

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

    public static List<PortalEntity> getPortalsAlongRay(Level level, Vec3 from, Vec3 to, Predicate<PortalEntity> filter) {
        List<PortalEntity> portalChain = new ArrayList<>();
        from = from.clone();
        to = to.clone();

        int limit = 100;
        while(limit-- > 0) {
            AABB rayAABB = new AABB(from.to3d(), to.to3d());

            Vec3 finalFrom = from.clone();
            Optional<PortalEntity> optionalPortal = PortalEntity.getPortals(level, rayAABB, filter)
                    .stream().reduce((o, n) ->
                            n.position().distanceTo(finalFrom.to3d()) < o.position().distanceTo(finalFrom.to3d()) ? n : o);

            if(!optionalPortal.isPresent())
                break;

            PortalEntity portal = optionalPortal.get();
            AABB clipAABB = portal.getBoundingBox().move(new Vec3(portal.getNormal()).mul(-1/16f).to3d());
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

    /**
     * Block ray that steps through portal pairs the same way as {@code Entity.pick} (see
     * {@link net.portalmod.mixins.entity.EntityMixin#pmPickThroughPortal}): if the segment crosses
     * portals, the trace continues from the linked exit. Use for visibility from a
     * {@link net.minecraft.client.Camera} that may already be on the far side of a portal
     * (nested {@code renderLevel}).
     */
    public static BlockHitResult clipThroughPortals(Level level, ClipContext context) {
        List<PortalEntity> portalChain = getPortalsAlongRay(level, new Vec3(context.getFrom()), new Vec3(context.getTo()), portal -> true);

        if(portalChain.isEmpty())
            return level.clip(context);

        BlockHitResult normalRay = level.clip(context);
        net.minecraft.world.phys.Vec3 positionBeforePortal = normalRay.getLocation();
        Optional<net.minecraft.world.phys.Vec3> optionalPositionOnPortal = portalChain.get(0).getBoundingBox().clip(context.getFrom(), context.getTo());

        if(optionalPositionOnPortal.isPresent()) {
            double distanceBeforePortal = positionBeforePortal.subtract(context.getFrom()).length();
            double distanceOnPortal = optionalPositionOnPortal.get().subtract(context.getFrom()).length();

            if(distanceBeforePortal < distanceOnPortal)
                return normalRay;
        }

        Mat4 portalMatrix = getMatrixFromPortalChain(portalChain);
        net.minecraft.world.phys.Vec3 to = new Vec3(context.getTo()).transform(portalMatrix).to3d();
        net.minecraft.world.phys.Vec3 from = new Vec3(context.getFrom()).transform(portalMatrix).to3d();

        PortalEntity last = portalChain.get(portalChain.size() - 1);
        if(!last.getOtherPortal().isPresent())
            return level.clip(context);

        Optional<net.minecraft.world.phys.Vec3> intersection = last.getOtherPortal().get().getBoundingBox().clip(from, to);
        if(intersection.isPresent())
            from = intersection.get();

        ClipContext transformed = new RayTraceContextWrapper(context);
        ((RayTraceContextWrapper)transformed).setFrom(from);
        ((RayTraceContextWrapper)transformed).setTo(to);
        return level.clip(transformed);
    }

    public static Mat4 getMatrixFromPortalChain(List<PortalEntity> portalChain) {
        Mat4 portalMatrix = Mat4.identity();

        for(PortalEntity portal : portalChain) {
            if(!portal.getOtherPortal().isPresent())
                break;

            Mat4 matrix = portal.getSourceBasis().getChangeOfBasisMatrix(portal.getOtherPortal().get().getDestinationBasis());

            portalMatrix = Mat4.identity()
                    .translate(new Vec3(portal.getOtherPortal().get().position()))
                    .mul(matrix)
                    .translate(new Vec3(portal.position()).negate())
                    .mul(portalMatrix);
        }

        return portalMatrix;
    }

    public static Mat4 getRotationMatrixFromPortalChain(List<PortalEntity> portalChain) {
        Mat4 portalMatrix = Mat4.identity();

        for(PortalEntity portal : portalChain) {
            if(!portal.getOtherPortal().isPresent())
                break;

            Mat4 matrix = portal.getSourceBasis().getChangeOfBasisMatrix(portal.getOtherPortal().get().getDestinationBasis());
            portalMatrix = Mat4.identity().mul(matrix).mul(portalMatrix);
        }

        return portalMatrix;
    }

    public static Pair<net.minecraft.world.phys.Vec3, net.minecraft.world.phys.Vec3> teleportRay(List<PortalEntity> portalChain, net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) {
        if(portalChain.isEmpty())
            return new Pair<>(from, to);

        // teleport ray
        Mat4 portalMatrix = getMatrixFromPortalChain(portalChain);
        from = new Vec3(from).transform(portalMatrix).to3d();
        to = new Vec3(to).transform(portalMatrix).to3d();

        // get last portal
        PortalEntity last = portalChain.get(portalChain.size() - 1);
        if(!last.getOtherPortal().isPresent())
            return new Pair<>(from, to);

        // snip ray
        Optional<net.minecraft.world.phys.Vec3> intersection = last.getOtherPortal().get().getBoundingBox().clip(from, to);
        if(intersection.isPresent())
            from = intersection.get();

        return new Pair<>(from, to);
    }

    public static net.minecraft.world.phys.Vec3 getOldPos(Entity entity) {
        return new net.minecraft.world.phys.Vec3(entity.xo, entity.yo, entity.zo);
    }

    public static void addTooltip(String name, List<Component> list) {
        if (!PortalModConfigManager.TOOLTIPS.get()) {
            return;
        }

        if (!Screen.hasControlDown()) {
            list.add(tooltipComponent("tooltip.portalmod.hold_control", getModifierKeyName()));
            return;
        }

        // Single line
        if (I18n.exists("tooltip.portalmod." + name)) {
            list.add(tooltipComponent("tooltip.portalmod." + name));
            return;
        }

        // Multi line
        int i = 1;
        while (true) {
            String key = "tooltip.portalmod." + name + "_" + i;
            if (!I18n.exists(key)) {
                break;
            }
            list.add(tooltipComponent(key));
            i++;
        }
    }

    public static MutableComponent tooltipComponent(String key, Object... args) {
        return Component.translatable(key, args).setStyle(TOOLTIP_STYLE);
    }

    private static String getModifierKeyName() {
        return Util.getPlatform() == Util.OS.OSX
                ? "Command"
                : "Ctrl";
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

    public static void sendChat(Level level, Object... text) {
        sendChat(level, level.isClientSide, text);
    }

    private static void sendChat(Level level, boolean isClientSide, Object... text) {
        String formatted = Arrays.stream(text).map(t -> (t == null) ? "null" : t.toString()).collect(Collectors.joining(" "));

        try {
            level.players().forEach(
                    player -> player.displayClientMessage(Component.literal(
                            (isClientSide ? "§3§l[Client " : "§7§l[Server ") + String.format("%03d", lastChatNumber) + "]: §r" + formatted
                    ), false)
            );
        } catch (Exception e) {
            PortalMod.LOGGER.error("Could not send debug chat message", e);
        }

        lastChatNumber = (lastChatNumber + 1) % 1000;
    }

    public static boolean canPlaceAt(BlockPlaceContext context, BlockPos pos) {
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

    /** 1.16 Entity#setPosAndOldPos: move the entity and reset its previous position (no interpolation). */
    public static void setPosAndOldPos(Entity entity, double x, double y, double z) {
        entity.setPos(x, y, z);
        entity.xo = x;
        entity.yo = y;
        entity.zo = z;
        entity.xOld = x;
        entity.yOld = y;
        entity.zOld = z;
    }
}
