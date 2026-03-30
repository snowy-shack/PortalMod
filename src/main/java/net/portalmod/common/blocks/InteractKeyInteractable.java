package net.portalmod.common.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.portalmod.core.init.AttributeInit;

public interface InteractKeyInteractable {
    boolean interactKeyInteract(PlayerEntity player, BlockRayTraceResult rayHit);

    default boolean withinInteractRange(PlayerEntity player, BlockRayTraceResult rayHit) {
        double buttonReach = player.getAttributeValue(AttributeInit.BUTTON_REACH.get());
        return rayHit.getLocation().subtract(player.getEyePosition(1)).length() < buttonReach;
    }
}
