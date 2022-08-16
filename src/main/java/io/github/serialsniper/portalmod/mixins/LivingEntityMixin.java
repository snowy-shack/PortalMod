package io.github.serialsniper.portalmod.mixins;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
//    @Inject(at = @At(value = "TAIL"), method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V")
//    private void travel(Vector3d vector, CallbackInfo info) {
//        if(((LivingEntity)(Object)this).level.isClientSide())
//            return;

//        if((LivingEntity)(Object)this instanceof PlayerEntity)
//            System.out.println(launched);
//        if(launched) {
//            ((Entity)(Object)this).setDeltaMovement(0, 0, 0);
//            ((Entity)(Object)this).setPos(
//                    launchedPos.x + (tickCount - launchedTick) * .1,
//                    launchedPos.y + parabola((tickCount - launchedTick) * .1),
//                    launchedPos.z
//            );
//        }
//    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setDeltaMovement(DDD)V"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/util/math/vector/Vector3d;F)Lnet/minecraft/util/math/vector/Vector3d;")
    ), method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V")
    private void portalmod_travel(LivingEntity instance, double x, double y, double z) {
        if(portalmod_launched)
            instance.setDeltaMovement(instance.getDeltaMovement().x, instance.getDeltaMovement().y - 0.08, instance.getDeltaMovement().z);
        else
            instance.setDeltaMovement(x, y, z);
    }

//    private double parabola(double ticks) {
//        return -0.254260120548 * ticks * ticks + 1.5223545069 * ticks;
//    }
}