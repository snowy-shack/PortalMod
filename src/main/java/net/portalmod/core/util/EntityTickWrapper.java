package net.portalmod.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tuple;
import net.portalmod.core.interfaces.ITeleportLerpable;

import java.util.function.Consumer;

public class EntityTickWrapper {
    public static void wrapTick(Entity entity, Consumer<Entity> action) {
        action.accept(entity);
        if(!entity.level.isClientSide && !(entity instanceof PlayerEntity))
            ((ITeleportLerpable)entity).getLerpPositions()
                    .add(new Tuple<>(ModUtil.getOldPos(entity), entity.position()));
    }
}