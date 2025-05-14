package net.portalmod.common.sorted.portal;

import com.google.common.collect.Lists;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.*;
import net.portalmod.core.util.ModUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalPlacer {
    public static PortalEntity placePortal(World level, PortalEnd end, String hue, UUID gunUUID, Vec3 position, Direction face, Direction upDirection) {
        Vec3 forward = new Vec3(face);
        Vec3 up = new Vec3(upDirection);
        Vec3 right = up.clone().cross(forward);

        Mat4 toAbsolute = new Mat4(
                right.x, up.x, forward.x, 0,
                right.y, up.y, forward.y, 0,
                right.z, up.z, forward.z, 0,
                0, 0, 0, 1
        );

        // Portal hitbox AABB
        AxisAlignedBB smallAABB = new AxisAlignedBB(-.5, -1, 0,  .5,  1, 1/16f);
        smallAABB = AABBUtil.transform(smallAABB, toAbsolute);
        smallAABB = AABBUtil.translate(smallAABB, position);

        // probably to get the blocks behind
        AxisAlignedBB largeAABB = new AxisAlignedBB(-1, -2, -(1/16f - .001),  1,  2, -.001);
        largeAABB = AABBUtil.transform(largeAABB, toAbsolute);
        largeAABB = AABBUtil.translate(largeAABB, position);

        boolean skipFrontBlock = position.clone()
                .add(new Vec3(face.getNormal()).mul(1/16f - .001))
                .to3i().equals(position.clone().sub(new Vec3(face.getNormal()).mul(.001)).to3i());

        // Get all blocks TODO surface / blocking?
        List<BlockPos> blocks = Lists.newArrayList();
        for (int z = (int) Math.floor(largeAABB.minZ); z <= (int) Math.floor(largeAABB.maxZ); z++)
            for (int y = (int) Math.floor(largeAABB.minY); y <= (int) Math.floor(largeAABB.maxY); y++)
                for (int x = (int) Math.floor(largeAABB.minX); x <= (int) Math.floor(largeAABB.maxX); x++)
                    blocks.add(new BlockPos(x, y, z));

        // Prepare a VoxelShape with all portalable surface blocks
        VoxelShape surface = VoxelShapes.empty();
        for (BlockPos block : blocks) {
            // TODO ?? Why negate
            if (!PortalEntity.canSurviveOn(level, block, face, skipFrontBlock)) continue;

            // Get the VoxelShape of the block
            VoxelShape blockShape = level.getBlockState(block).getCollisionShape(level, block);

            // Find surface cubes in the Block shape, and add them to the surface VoxelShape
            for (AxisAlignedBB aabb : blockShape.toAabbs()) {
                aabb = aabb.move(block);

                if (AABBUtil.getSide(aabb, face) == position.choose(face.getAxis())) {
                    VoxelShape aabbShape = VoxelShapes.create(aabb);
                    surface = VoxelShapes.or(surface, aabbShape);
                }
            }
        }

        // Create an extruded VoxelShape of invalid surface blocks to reposition the portal
        // TODO add preexisting portals to this to add Portal bumping
        VoxelShape collision = VoxelShapes.empty();
        for (BlockPos block : blocks) {
            if (!PortalEntity.canSurviveOn(level, block, face, skipFrontBlock))
                collision = VoxelShapes.or(collision, VoxelShapes.create(new AxisAlignedBB(block)));

            VoxelShape blockShape = level.getBlockState(block).getCollisionShape(level, block);
            for (AxisAlignedBB aabb : blockShape.toAabbs()) {
                aabb = aabb.move(block);

                // Check whether the AABB is extending beyond the surface (rather than being inset)
                boolean isBump = face.getAxisDirection() == Direction.AxisDirection.POSITIVE
                        ? AABBUtil.getSide(aabb, face) > position.choose(face.getAxis())
                        : AABBUtil.getSide(aabb, face) < position.choose(face.getAxis());

                // If it's a bump, extrude it and add it to the collision shape
                if (isBump) {
//                    Vec3 extrusion = new Vec3(face.getNormal()).mul(1000);
                    VoxelShape aabbShape = VoxelShapes.create(aabb);
//                            .expandTowards(extrusion.to3d()) // TODO Re-enable if bumps aren't correctly recognized
//                            .expandTowards(extrusion.negate().to3d()));

                    collision = VoxelShapes.or(collision, aabbShape);
                }
            }
        }

        // Carve out the extruded invalid VoxelShapes from the surface shape
        surface = VoxelShapes.join(surface, collision, IBooleanFunction.ONLY_FIRST);

        // # Bumping
        // get valid corners and largest reaction per direction
        // reaction := movement of the portal to react to invalid surfaces
        // tldr moving the portal towards the nearest valid surface
        List<AABBVertex> corners = Collider.getFaceCorners(face.getOpposite(), smallAABB); // Size: 4, all DOWN
        Map<Direction, Double> largestReactionByDirection = new HashMap<>();
        for (AxisAlignedBB box : collision.toAabbs()) {
            for (int i = corners.size() - 1; i >= 0; i--)
                if (Collider.pointInBoxExceptAxis(face.getAxis(), box, corners.get(i).getPosition()))
                    corners.remove(i);

            Optional<List<Vec3>> reactionsOptional = Collider.collideExceptAxis(face.getAxis(), box, smallAABB);
            if (!reactionsOptional.isPresent()) continue;

            List<Vec3> reactions = reactionsOptional.get();

            for (Vec3 reaction : reactions) {
                Direction reactionDirection = reaction.clone().normalize().round().toDirection();
                if (largestReactionByDirection.containsKey(reactionDirection)) {
                    if (reaction.magnitude() > largestReactionByDirection.get(reactionDirection)) {
                        largestReactionByDirection.put(reactionDirection, reaction.magnitude());
                    }
                } else {
                    largestReactionByDirection.put(reactionDirection, reaction.magnitude());
                }
            }
        }

        Vec3 reaction = null;

        List<List<Direction>> allNormals = corners.stream()
                .map(corner -> corner.getCorner().getNormals())
                .collect(Collectors.toList());

        allNormals.forEach(normals -> normals.remove(face.getOpposite()));

        ModUtil.sendChatSinglePlayer("Corners", corners.size());
        switch (corners.size()) {
            // One corner is unobstructed
            case 1: {
//                for (Direction direction : Direction.values())
//                    reaction.add(largestReactionByDirection.get(direction));

                reaction = allNormals.get(0).stream().map(direction -> {
                    if (largestReactionByDirection.containsKey(direction))
                        return new Vec3(direction).mul(largestReactionByDirection.get(direction));
                    return Vec3.origin();
                }).reduce(Vec3.origin(), Vec3::add);
                break;
            }

            // Two corners are unobstructed
            case 2: {
                List<Direction> reactionDirections = allNormals.get(0).stream().filter(allNormals.get(1)::contains).collect(Collectors.toList());
                if (reactionDirections.isEmpty()) return null;

                Direction reactionDirection = reactionDirections.get(0);
                reaction = new Vec3(reactionDirection).mul(largestReactionByDirection.get(reactionDirection));
                break;
            }

            // Three corners are unobstructed
            case 3: {
                Stream<Direction> normals12 = allNormals.get(0).stream().filter(allNormals.get(1)::contains);
                Stream<Direction> normals23 = allNormals.get(1).stream().filter(allNormals.get(2)::contains);
                Stream<Direction> normals13 = allNormals.get(0).stream().filter(allNormals.get(2)::contains);

                reaction = Stream.concat(Stream.concat(normals12, normals23), normals13)
                        .map(direction -> new Vec3(direction).mul(largestReactionByDirection.get(direction)))
                        .reduce(Vec3.infinity(), (previous, next) ->
                                next.magnitude() < previous.magnitude()
                                        ? next : previous
//                                next.add(previous) // TODO the wise Snipy said this caused bugs. INVESTIGATE!!
                        );
                break;
            }

            // All corners are unobstructed
            case 4: break;
            default: return null;
        }

        // Move the Portal position
        if (reaction != null) {
            position.add(reaction);
            smallAABB = AABBUtil.translate(smallAABB, reaction);
        }

        // Check whether the new Portal position is still invalid
        Vec3 extrusion = new Vec3(face.getNormal()).mul(1000);
        surface = AABBUtil.forEachBox(surface, box ->
                box.expandTowards(extrusion.to3d()).expandTowards(extrusion.negate().to3d()));
        extrusion.div(2);
        VoxelShape solid = VoxelShapes.create(
                largeAABB.inflate(10).expandTowards(extrusion.to3d()).expandTowards(extrusion.negate().to3d()));

        VoxelShape invertedSurface = VoxelShapes.join(surface, solid, IBooleanFunction.ONLY_SECOND);

        // Snap the Portal position to texels
        position.mul(16).round().div(16);

        smallAABB = smallAABB.deflate(.001);
        AxisAlignedBB finalSmallAABB = smallAABB;
        boolean stillCollides = invertedSurface.toAabbs().stream().anyMatch(box ->
                Collider.collideExceptAxis(face.getAxis(), box, finalSmallAABB).isPresent());

        if (stillCollides) return null;

        // Check if all blocks below the portal are still portalable
        boolean canSurvive = AABBUtil.checkBlocksWithin(level,
                smallAABB.move(new Vec3(face.getNormal()).mul(-1/16f).to3d()),
                (pos, state) -> PortalEntity.canSurviveOn(level, pos, face, skipFrontBlock)
        );

        if (!canSurvive) return null;

        // Spawn the portal
        PortalEntity portal = new PortalEntity(level);
        position.add(new Vec3(face.getNormal()).mul(.001));
        portal.setPos(position.x, position.y, position.z);
        portal.setDirection(face);
        portal.setUpVector(up.toDirection());
        portal.setEnd(end);
        portal.setHue(hue);
        portal.setGunUUID(gunUUID);

        PortalManager.put(gunUUID, end, portal);
        level.addFreshEntity(portal);

        level.playSound(null, portal.getX(), portal.getY(), portal.getZ(),
                SoundInit.PORTAL_OPEN.get(), SoundCategory.NEUTRAL, 1f, 1);

        return portal;
    }
}