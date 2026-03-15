package net.portalmod.common.sorted.portal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.common.blocks.PortalableBlock;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.common.sorted.panel.PortalHelper;
import net.portalmod.common.sorted.portalgun.PortalGunClient;
import net.portalmod.core.init.BlockTagInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalPlacer {
    private static final BinaryOperator<Vec3> selectLeast = (o, n) -> n.magnitude() < o.magnitude() ? n : o;

    public static PortalEntity placePortal(World level, PortalEnd end, String hue, UUID gunUUID, Vec3 position, Direction face, Direction upDirection, boolean override, @Nullable Direction[] lookingDirections) {
        Vec3 forward = new Vec3(face);
        Vec3 up = new Vec3(upDirection);
        Vec3 right = up.clone().cross(forward);
        Mat4 toAbsolute = new OrthonormalBasis(right, up).getChangeOfBasisFromCanonicalMatrix();
        BlockPos shotBlockPos = position.clone().add(new Vec3(face.getOpposite()).mul(0.001)).toBlockPos();
        BlockState shotBlockState = level.getBlockState(shotBlockPos);
        BlockState behindBlockState = level.getBlockState(shotBlockPos.relative(face.getOpposite()));
        Block shotBlock = shotBlockState.getBlock();

        if(!(PortalableBlock.isPortalable(shotBlockState, face, level)
                || shotBlock.is(BlockTagInit.PORTAL_INHERITING) && PortalableBlock.isPortalable(behindBlockState, face, level)))
            return null;

        if(PortalGunClient.getInstance().willBeHelped(gunUUID, end, shotBlockPos, face, upDirection, level) && lookingDirections != null) {
            PortalGunClient.getInstance().setHelped(gunUUID, end, shotBlockPos, face);
            Pair<Vec3, Direction> helpment = ((PortalHelper)shotBlock).helpPortal(position, face, upDirection, lookingDirections, shotBlockState, level);
            position = helpment.getFirst();
            up = new Vec3(helpment.getSecond());
        }

        AxisAlignedBB portalAABB = new AxisAlignedBB(-0.5, -1, 0,0.5, 1, 1/16f);
        portalAABB = AABBUtil.transform(portalAABB, toAbsolute).move(position.to3d());
        VoxelShape collision = getCollision(level, face, position, toAbsolute, true);

        Vec3 finalPosition = position;
        List<PortalEntity> portalsInTheWay = PortalEntity.getPortals(level, portalAABB.inflate(2),
                portal -> new Vec3(portal.getNormal()).dot(new Vec3(face)) > 0.99
                        && new Vec3(portal.position()).choose(face.getAxis()) - finalPosition.choose(face.getAxis()) < 0.01
                        && !(portal.getGunUUID().equals(gunUUID) && portal.getEnd() == end));
        List<PortalEntity> portalsInTheWay2 = PortalEntity.getPortals(level, portalAABB,
                portal -> new Vec3(portal.getNormal()).dot(new Vec3(face)) > 0.99
                        && new Vec3(portal.position()).choose(face.getAxis()) - finalPosition.choose(face.getAxis()) < 0.01
                        && !(portal.getGunUUID().equals(gunUUID) && portal.getEnd() == end));
        List<AxisAlignedBB> bumpingPortals = portalsInTheWay.stream().map(Entity::getBoundingBox).collect(Collectors.toList());

        if(!override) {
            collision = AABBUtil.addBoxesToVoxelShape(collision, bumpingPortals).optimize();
        }

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
                        .reduce(new Vec3(1e128), selectLeast);
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
        if(portalCollides(face, portalAABB, collision))
            return null;

        if(override) {
            portalsInTheWay2.forEach(portal -> PortalManager.getInstance().scheduleRemoval(portal));
        }

        // Spawn the portal
        PortalEntity portal = new PortalEntity(level);
        position.add(new Vec3(face.getNormal()).mul(.001));
        portal.setPos(position.x, position.y, position.z);
        portal.setDirection(face);
        portal.setUpVector(up.toDirection());
        portal.setEnd(end);
        portal.setHue(hue);
        portal.setGunUUID(gunUUID);
        portal.recalculateBoundingBox();

        PortalEntity oldPortal = PortalManager.getInstance().get(gunUUID, end);
        PortalManager.getInstance().put(gunUUID, end, portal);
        level.addFreshEntity(portal);

        PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new SPortalShotPacket(portal.getId(), oldPortal == null ? -1 : oldPortal.getId()));

        level.playSound(null, portal.getX(), portal.getY(), portal.getZ(),
                SoundInit.PORTAL_OPEN.get(), SoundCategory.NEUTRAL, 1f, 1);

        return portal;
    }

    public static VoxelShape getCollision(World level, Direction face, Vec3 position, Mat4 toAbsolute, boolean large) {
        AxisAlignedBB portalWideAABB = new AxisAlignedBB(-0.5, -1, 0,0.5, 1, 1/16f)
                        .deflate(0.002);

        if(large) {
            portalWideAABB = portalWideAABB
                    .expandTowards(-2, -2, 0)
                    .expandTowards(2, 2, 0);
        }

        AxisAlignedBB behind = portalWideAABB.move(0, 0, -1/16f);
        behind = AABBUtil.transform(behind, toAbsolute).move(position.to3d());
        AxisAlignedBB front = AABBUtil.transform(portalWideAABB, toAbsolute).move(position.to3d());

        List<BlockPos> backBlocks = AABBUtil.getBlocksWithin(behind);
        List<BlockPos> frontBlocks = AABBUtil.getBlocksWithin(front);
        VoxelShape backCollision = VoxelShapes.empty();
        VoxelShape frontCollision = VoxelShapes.empty();

        for(BlockPos block : backBlocks) {
            BlockState attachedBlock = level.getBlockState(block);
            BlockState behindBlock = level.getBlockState(block.relative(face.getOpposite()));

            boolean portalable = PortalableBlock.isPortalable(attachedBlock, face, level);
            boolean inheriting = attachedBlock.is(BlockTagInit.PORTAL_INHERITING);
            boolean behindPortalable = PortalableBlock.isPortalable(behindBlock, face, level);
            boolean valid = portalable || (inheriting && behindPortalable);

            VoxelShape blockShape = level.getBlockState(block)
                    .getShape(level, block)
                    .move(block.getX(), block.getY(), block.getZ());

            VoxelShape fullBlockShape = VoxelShapes.block().move(block.getX(), block.getY(), block.getZ());
            VoxelShape airAround = VoxelShapes.joinUnoptimized(fullBlockShape, blockShape, IBooleanFunction.ONLY_FIRST);

            backCollision = VoxelShapes.joinUnoptimized(backCollision, valid ? airAround : fullBlockShape, IBooleanFunction.OR);
        }

        for(BlockPos block : frontBlocks) {
            BlockState frontBlock = level.getBlockState(block);

            boolean frontNonBlocking = frontBlock.is(BlockTagInit.PORTAL_NONBLOCKING);

            VoxelShape blockShape = level.getBlockState(block)
                    .getShape(level, block)
                    .move(block.getX(), block.getY(), block.getZ());

            if(frontBlock.getBlock() instanceof AbstractGelBlock) {
                if(frontBlock.getValue(AbstractGelBlock.STATES.get(face.getOpposite()))) {
                    blockShape = AbstractGelBlock.SHAPES.get(face.getOpposite()).getShape()
                            .move(block.getX(), block.getY(), block.getZ());
                } else {
                    blockShape = VoxelShapes.empty();
                }
            }

            if(!frontNonBlocking)
                frontCollision = VoxelShapes.joinUnoptimized(frontCollision, blockShape, IBooleanFunction.OR);
        }

        backCollision = VoxelShapes.joinUnoptimized(backCollision, VoxelShapes.create(behind), IBooleanFunction.AND);
        frontCollision = VoxelShapes.joinUnoptimized(frontCollision, VoxelShapes.create(front), IBooleanFunction.AND);
        return VoxelShapes.or(backCollision, frontCollision);
    }

    // reaction := movement of the portal to move out of invalid surfaces
    private static Map<Direction, Double> getLargestReactionByDirection(Direction face, AxisAlignedBB portalAABB, VoxelShape collision) {
        Map<Direction, Double> largestReactionByDirection = new HashMap<>();

        for(AxisAlignedBB box : collision.toAabbs()) {
            if(!Collider.intersectExceptAxis(face.getAxis(), box, portalAABB))
                continue;

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

    public static boolean portalCollides(Direction face, AxisAlignedBB portalAABB, VoxelShape collision) {
        return collision.toAabbs().stream().anyMatch(box ->
                Collider.collideExceptAxis(face.getAxis(), box, portalAABB).isPresent());
    }
}
