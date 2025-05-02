package net.portalmod.core.math;

import com.google.common.collect.Lists;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Collider {
    public static Optional<Vec3> collide(Direction.Axis axis, AxisAlignedBB collider, AxisAlignedBB colliding) {
        double collision1 = colliding.max(axis) - collider.min(axis);
        double collision2 = collider.max(axis) - colliding.min(axis);

        if(collision1 <= 0 || collision2 <= 0)
            return Optional.empty();

        Vec3 reaction = Vec3.fromAxis(axis);

        if(collision1 < collision2) {
            reaction.negate().mul(collision1);
        } else {
            reaction.mul(collision2);
        }

        return Optional.of(reaction);
    }

    public static Optional<List<Vec3>> collideExceptAxis(@Nullable Direction.Axis axis, AxisAlignedBB collider, AxisAlignedBB colliding) {
        List<Vec3> reactions = Lists.newArrayList();
        for(Direction.Axis a : Direction.Axis.values()) {
            if(a == axis)
                continue;

            Optional<Vec3> reaction = Collider.collide(a, collider, colliding);
            if(!reaction.isPresent())
                return Optional.empty();
            reactions.add(reaction.get());
        }
        return Optional.of(reactions);
    }

    public static boolean pointInBox(Direction.Axis axis, AxisAlignedBB collider, Vec3 point) {
        return point.choose(axis) > collider.min(axis) && point.choose(axis) < collider.max(axis);
    }

    public static boolean pointInBoxExceptAxis(Direction.Axis axis, AxisAlignedBB collider, Vec3 point) {
        for(Direction.Axis a : Direction.Axis.values()) {
            if(a == axis)
                continue;
            if(!pointInBox(a, collider, point))
                return false;
        }
        return true;
    }

    public static List<List<Integer>> listCombinations(int base, int digits) {
        List<List<Integer>> combinations = Lists.newArrayList();
        recurseCombinations(combinations, base, digits, 0, new Stack<>());
        return combinations;
    }

    private static void recurseCombinations(List<List<Integer>> combinations, int base, int digits, int depth, Stack<Integer> currentCombination) {
        for(int i = 0; i < base; i++) {
            currentCombination.push(i);
            if(depth < digits - 1) {
                recurseCombinations(combinations, base, digits, depth + 1, currentCombination);
            } else {
                combinations.add(currentCombination);
            }
            currentCombination.pop();
        }
    }

    public static <A, B, R> List<R> combineFunctions(A[] as, Function<A, B>[] fs, Function<B[], R> aggregator) {
        List<List<Integer>> combinations = listCombinations(fs.length, as.length);
        List<R> combined = Lists.newArrayList();

        for(List<Integer> combination : combinations) {
            B[] bs = (B[])(new Object[as.length]);
            for(int j = 0; j < as.length; j++) {
                A a = as[j];
                B b = fs[combination.get(j)].apply(a);
                bs[j] = b;
            }
            combined.add(aggregator.apply(bs));
        }

        return combined;
    }

    public static List<AABBVertex> getVertices(AxisAlignedBB box) {
//        return Collider.<Direction.Axis, Double, Vec3>combineFunctions(
//                Direction.Axis.values(),
//                new Function[] {
//                        axis -> box.min((Direction.Axis)axis),
//                        axis -> box.max((Direction.Axis)axis)
//                },
//                x -> new Vec3(x[0], x[1], x[2])
//        );

        return Lists.newArrayList(
                new AABBVertex(new Vec3(box.minX, box.minY, box.minZ), AABBVertex.Corner.LEFT_DOWN_FORWARDS),
                new AABBVertex(new Vec3(box.maxX, box.minY, box.minZ), AABBVertex.Corner.RIGHT_DOWN_FORWARDS),
                new AABBVertex(new Vec3(box.minX, box.maxY, box.minZ), AABBVertex.Corner.LEFT_UP_FORWARDS),
                new AABBVertex(new Vec3(box.maxX, box.maxY, box.minZ), AABBVertex.Corner.RIGHT_UP_FORWARDS),
                new AABBVertex(new Vec3(box.minX, box.minY, box.maxZ), AABBVertex.Corner.LEFT_DOWN_BACKWARDS),
                new AABBVertex(new Vec3(box.maxX, box.minY, box.maxZ), AABBVertex.Corner.RIGHT_DOWN_BACKWARDS),
                new AABBVertex(new Vec3(box.minX, box.maxY, box.maxZ), AABBVertex.Corner.LEFT_UP_BACKWARDS),
                new AABBVertex(new Vec3(box.maxX, box.maxY, box.maxZ), AABBVertex.Corner.RIGHT_UP_BACKWARDS)
        );
    }

    public static List<AABBVertex> getFaceCorners(Direction direction, AxisAlignedBB box) {
        Direction.Axis axis = direction.getAxis();
        return getVertices(box).stream().filter(v -> v.getPosition().choose(axis) == AABBUtil.getSide(box, direction)).collect(Collectors.toList());
    }
}