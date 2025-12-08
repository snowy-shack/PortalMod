package net.portalmod.common.sorted.portal;

import net.minecraft.entity.Entity;
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

import java.util.*;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalPlacer {
    private static final BinaryOperator<Vec3> selectLeast = (o, n) -> n.magnitude() < o.magnitude() ? n : o;

    public static PortalEntity placePortal(World level, PortalEnd end, String hue, UUID gunUUID, Vec3 position, Direction face, Direction upDirection) {
        Vec3 forward = new Vec3(face);
        Vec3 up = new Vec3(upDirection);
        Vec3 right = up.clone().cross(forward);

        Mat4 toAbsolute = new OrthonormalBasis(right, up).getChangeOfBasisFromCanonicalMatrix();

        AxisAlignedBB portalAABB = new AxisAlignedBB(-.5, -1, 0,  .5,  1, 1/16f);
        portalAABB = AABBUtil.transform(portalAABB, toAbsolute);
        portalAABB = AABBUtil.translate(portalAABB, position);

        boolean skipFrontBlock = position.clone()
                .add(new Vec3(face.getNormal()).mul(1/16f - .001))
                .to3i().equals(position.clone().sub(new Vec3(face.getNormal()).mul(.001)).to3i());

        VoxelShape collision = getCollision(level, face, position, toAbsolute, skipFrontBlock);

        List<AxisAlignedBB> bumpingPortals = PortalEntity.getPortals(level, portalAABB.inflate(2),
                portal -> new Vec3(portal.getNormal()).dot(new Vec3(face)) > 0.99
                        && new Vec3(portal.position()).choose(face.getAxis()) - position.choose(face.getAxis()) < 0.01
                        && !(portal.getGunUUID().equals(gunUUID) && portal.getEnd() == end))
                .stream().map(Entity::getBoundingBox).collect(Collectors.toList());

        collision = AABBUtil.addBoxesToVoxelShape(collision, bumpingPortals);

        // get vertices on valid surface
        List<AABBVertex> vertices = Collider.getFaceCorners(face.getOpposite(), portalAABB);
        for(AxisAlignedBB box : collision.toAabbs())
            for(int i = vertices.size() - 1; i >= 0; i--)
                if(Collider.pointInBoxExceptAxis(face.getAxis(), box, vertices.get(i).getPosition()))
                    vertices.remove(i);

        List<List<Direction>> allNormals = vertices.stream()
                .map(corner -> corner.getCorner().getNormals())
                .collect(Collectors.toList());
        allNormals.forEach(normals -> normals.remove(face.getOpposite()));

        Vec3 finalReaction = null;

        // number of vertices on valid surface
        switch(vertices.size()) {
            case 1: {
                Map<Direction, Double> largestReactionByDirection =
                        getLargestReactionByShortestDirection(face, portalAABB, collision, allNormals);

                finalReaction = allNormals.get(0).stream().map(direction ->
                        new Vec3(direction).mul(largestReactionByDirection.getOrDefault(direction, 0d)))
                                .reduce(Vec3.origin(), Vec3::add);
                break;
            }

            case 2: {
                List<Direction> reactionDirections = allNormals.get(0).stream()
                        .filter(allNormals.get(1)::contains)
                        .collect(Collectors.toList());

                if(reactionDirections.isEmpty())
                    return null;

                Map<Direction, Double> largestReactionByDirection = getLargestReactionByDirection(face, portalAABB, collision);

                Direction reactionDirection = reactionDirections.get(0);
                finalReaction = new Vec3(reactionDirection).mul(largestReactionByDirection.get(reactionDirection));
                break;
            }

            case 3: {
                Stream<Direction> normals12 = allNormals.get(0).stream().filter(allNormals.get(1)::contains);
                Stream<Direction> normals23 = allNormals.get(1).stream().filter(allNormals.get(2)::contains);
                Stream<Direction> normals13 = allNormals.get(0).stream().filter(allNormals.get(2)::contains);

                Map<Direction, Double> largestReactionByDirection = getLargestReactionByDirection(face, portalAABB, collision);

                finalReaction = Stream.concat(Stream.concat(normals12, normals23), normals13)
                        .map(direction -> new Vec3(direction).mul(largestReactionByDirection.get(direction)))
                        .reduce(Vec3.origin(), selectLeast);
                break;
            }

            case 4: break;

            default: return null;
        }

        // Move the Portal position
        if(finalReaction != null) {
            position.add(finalReaction);
            portalAABB = AABBUtil.translate(portalAABB, finalReaction);
        }

        // Snap the Portal position to texels
        position.mul(16).round().div(16);

        portalAABB = portalAABB.deflate(.001);

        // Check whether the new Portal position is still invalid
        if(stillCollides(face, portalAABB, collision))
            return null;

        // Check if all blocks behind the portal are valid
        if(!canSurvive(level, face, portalAABB, skipFrontBlock))
            return null;

        // Spawn the portal
        PortalEntity portal = new PortalEntity(level);
        position.add(new Vec3(face.getNormal()).mul(.001));
        portal.setPos(position.x, position.y, position.z);
        portal.setDirection(face);
        portal.setUpVector(up.toDirection());
        portal.setEnd(end);
        portal.setHue(hue);
        portal.setGunUUID(gunUUID);

        PortalManager.getInstance().put(gunUUID, end, portal);
        level.addFreshEntity(portal);

        level.playSound(null, portal.getX(), portal.getY(), portal.getZ(),
                SoundInit.PORTAL_OPEN.get(), SoundCategory.NEUTRAL, 1f, 1);

        return portal;
    }

    private static VoxelShape extrudeVoxelShape(VoxelShape shape, Vec3 normal) {
        return AABBUtil.boxesToVoxelShape(shape.toAabbs().stream()
                .map(aabb -> aabb
                        .expandTowards(normal.to3d())
                        .expandTowards(normal.negate().to3d()))
                .collect(Collectors.toList()));
    }

    private static VoxelShape getCollision(World level, Direction face, Vec3 position, Mat4 toAbsolute, boolean skipFrontBlock) {
        AxisAlignedBB surfaceAABB = new AxisAlignedBB(-2.5, -3, -(1/16f - .001),  2.5,  3, -.001);
        surfaceAABB = AABBUtil.transform(surfaceAABB, toAbsolute);
        surfaceAABB = AABBUtil.translate(surfaceAABB, position);

        AxisAlignedBB overSurfaceAABB = new AxisAlignedBB(-2.5, -3, (1/16f - .001),  2.5,  3, .001);
        overSurfaceAABB = AABBUtil.transform(overSurfaceAABB, toAbsolute);
        overSurfaceAABB = AABBUtil.translate(overSurfaceAABB, position);

        List<BlockPos> blocks = AABBUtil.getBlocksWithin(surfaceAABB);
        VoxelShape collision = VoxelShapes.empty();
        VoxelShape bumps = VoxelShapes.empty();
        VoxelShape insets = VoxelShapes.create(surfaceAABB);

        // Slicing blocks to get bumps and insets
        for(BlockPos block : blocks) {
            if(!PortalEntity.canSurviveOn(level, block, face, skipFrontBlock))
                collision = VoxelShapes.or(collision, VoxelShapes.create(new AxisAlignedBB(block)));

            VoxelShape blockShape = level.getBlockState(block)
                    .getCollisionShape(level, block)
                    .move(block.getX(), block.getY(), block.getZ());

            bumps = VoxelShapes.or(bumps, VoxelShapes.join(VoxelShapes.create(overSurfaceAABB), blockShape, IBooleanFunction.AND));
            insets = VoxelShapes.join(insets, blockShape, IBooleanFunction.ONLY_FIRST);
        }

        collision = VoxelShapes.or(collision, bumps);
        collision = VoxelShapes.or(collision, insets);
        collision = extrudeVoxelShape(collision, new Vec3(face.getNormal()).mul(10));
        return collision;
    }

    // reaction := movement of the portal to move out of invalid surfaces
    private static Map<Direction, Double> getLargestReactionByDirection(Direction face, AxisAlignedBB portalAABB, VoxelShape collision) {
        Map<Direction, Double> largestReactionByDirection = new HashMap<>();

        for(AxisAlignedBB box : collision.toAabbs()) {
            Optional<List<Vec3>> reactionsOptional = Collider.collideExceptAxis(face.getAxis(), box, portalAABB);
            if(!reactionsOptional.isPresent())
                continue;

            for(Vec3 reaction : reactionsOptional.get()) {
                Direction reactionDirection = reaction.clone().normalize().round().toDirection();

                double oldMax = largestReactionByDirection.getOrDefault(reactionDirection, reaction.magnitude());
                largestReactionByDirection.put(reactionDirection, Math.max(oldMax, reaction.magnitude()));
            }
        }

        return largestReactionByDirection;
    }

    private static Map<Direction, Double> getLargestReactionByShortestDirection(Direction face, AxisAlignedBB portalAABB, VoxelShape collision, List<List<Direction>> allNormals) {
        Map<Direction, Double> largestReactionByDirection = new HashMap<>();

        for(AxisAlignedBB box : collision.toAabbs()) {
            Optional<List<Vec3>> reactionsOptional = Collider.collideExceptAxis(face.getAxis(), box, portalAABB);
            if(!reactionsOptional.isPresent())
                continue;

            Optional<Vec3> reactionOptional = reactionsOptional.get().stream()
                    .filter(x -> allNormals.get(0).contains(x.clone().normalize().round().toDirection()))
                    .reduce(selectLeast);
            if(!reactionOptional.isPresent())
                continue;

            Vec3 reaction = reactionOptional.get();
            Direction reactionDirection = reaction.clone().normalize().round().toDirection();

            double oldMax = largestReactionByDirection.getOrDefault(reactionDirection, reaction.magnitude());
            largestReactionByDirection.put(reactionDirection, Math.max(oldMax, reaction.magnitude()));
        }

        return largestReactionByDirection;
    }

    private static boolean stillCollides(Direction face, AxisAlignedBB portalAABB, VoxelShape collision) {
        return collision.toAabbs().stream().anyMatch(box ->
                Collider.collideExceptAxis(face.getAxis(), box, portalAABB).isPresent());
    }

    private static boolean canSurvive(World level, Direction face, AxisAlignedBB portalAABB, boolean skipFrontBlock) {
        return AABBUtil.checkBlocksWithin(
                level,
                portalAABB.move(new Vec3(face.getNormal()).mul(-1/16f).to3d()),
                (pos, state) -> PortalEntity.canSurviveOn(level, pos, face, skipFrontBlock)
        );
    }
}
