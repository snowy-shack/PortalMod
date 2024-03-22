package net.portalmod.common.sorted.longfallboots;

import net.minecraft.entity.LivingEntity;

public class LongFallBootsClient {
    protected static LongFallBootsModel getLFBModel(LivingEntity entity) {
        return new LongFallBootsModel(entity);
    }
}