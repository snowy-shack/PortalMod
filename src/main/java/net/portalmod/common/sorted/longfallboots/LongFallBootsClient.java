package net.portalmod.common.sorted.longfallboots;

import net.minecraft.world.entity.LivingEntity;

public class LongFallBootsClient {
    protected static LongFallBootsModel getLFBModel(LivingEntity entity) {
        return new LongFallBootsModel(entity);
    }
}