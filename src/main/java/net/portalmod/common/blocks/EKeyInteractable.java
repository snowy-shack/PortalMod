package net.portalmod.common.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;

public interface EKeyInteractable {
    boolean eKeyInteract(PlayerEntity player, BlockRayTraceResult rayHit);
}
