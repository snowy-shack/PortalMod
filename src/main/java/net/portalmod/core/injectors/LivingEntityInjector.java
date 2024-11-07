package net.portalmod.core.injectors;

import net.minecraft.entity.LivingEntity;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;

public class LivingEntityInjector {
    public static void onPreTick(LivingEntity entity) {
        RepulsionGelBlock.onPreTick(entity);
    }
    public static void onPostTick(LivingEntity entity) {
        RepulsionGelBlock.onPostTick(entity);
    }
}