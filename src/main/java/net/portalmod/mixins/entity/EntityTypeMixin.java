package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.turret.TurretEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T extends Entity> extends net.minecraftforge.registries.ForgeRegistryEntry<EntityType<?>> {
    @Inject(
            method = "spawn(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/nbt/CompoundNBT;Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/server/ServerWorld;addFreshEntityWithPassengers(Lnet/minecraft/entity/Entity;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void spawn(ServerWorld world, CompoundNBT nbt, ITextComponent textComponent, PlayerEntity player, BlockPos pos, SpawnReason spawnReason, boolean p_220342_7_, boolean p_220342_8_, CallbackInfoReturnable<T> cir, T t) {
        if (t instanceof TurretEntity && player != null) {
            ((TurretEntity) t).onSpawnedByPlayer(player);
        }
        if (t instanceof Cube) {
            ((Cube) t).onSpawnedByPlayer();
        }
    }
}