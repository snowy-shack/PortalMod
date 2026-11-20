package net.portalmod.common.blocks;

import net.portalmod.core.init.AttributeInit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public interface InteractKeyInteractable {
    boolean interactKeyInteract(Player player, BlockHitResult rayHit);

    default boolean withinInteractRange(Player player, BlockHitResult rayHit) {
        double buttonReach = player.getAttributeValue(AttributeInit.BUTTON_REACH.get());
        return rayHit.getLocation().subtract(player.getEyePosition(1)).length() < buttonReach;
    }
}
