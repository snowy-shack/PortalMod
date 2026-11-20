package net.portalmod.mixins.entity;

import net.minecraft.world.item.ItemStack;

import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.turret.TurretEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T extends Entity> {
    // 1.20.1: the player is only passed to the ItemStack overload (used by spawn eggs and the
    // ModSpawnEggItem dispenser), which then forwards to the CompoundTag/Consumer overload.
    @Inject(
            method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;",
            at = @At("RETURN")
    )
    public void spawn(ServerLevel world, ItemStack stack, Player player, BlockPos pos, MobSpawnType spawnReason, boolean p_220342_7_, boolean p_220342_8_, CallbackInfoReturnable<T> cir) {
        T t = cir.getReturnValue();
        if (t instanceof TurretEntity && player != null) {
            ((TurretEntity) t).onSpawnedByPlayer(player);
        }
        if (t instanceof Cube) {
            ((Cube) t).onSpawnedByPlayer();
        }
    }
}
