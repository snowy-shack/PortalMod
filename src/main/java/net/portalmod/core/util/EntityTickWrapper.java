package net.portalmod.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.core.interfaces.ITeleportLerpable;

import java.util.Deque;
import java.util.function.Consumer;

public class EntityTickWrapper {
    public static void wrapTick(Entity entity, Consumer<Entity> action) {
        action.accept(entity);
        if(!entity.level.isClientSide && !(entity instanceof PlayerEntity)) {
            Deque<Tuple<Vector3d, Vector3d>> lerpPositions = ((ITeleportLerpable) entity).getLerpPositions();
            lerpPositions.add(new Tuple<>(ModUtil.getOldPos(entity), entity.position()));

            // prevent deque from growing indefinitely
            while(lerpPositions.size() > 10) {
                lerpPositions.removeFirst();
            }
        }
    }
}