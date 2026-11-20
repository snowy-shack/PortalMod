package net.portalmod.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/** An item that still lets blocks react to a sneak-right-click (Forge: Item#doesSneakBypassUse). */
public interface SneakBypassUse {
    boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player);
}
