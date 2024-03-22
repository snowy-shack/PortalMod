package net.portalmod.core.injectors;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerEntityInjector {
    static boolean wasFlying = false;

    public static void travel(PlayerEntity player) {
//        Vector3d delta = PortalEntity.teleportEntity(player, player.getDeltaMovement());
//        player.setBoundingBox(player.getBoundingBox().move(delta));
//        player.setLocationFromBoundingbox();
//        if(player.level.isClientSide) {
//            Method serverAiStepMethod = ObfuscationReflectionHelper.findMethod(ClientPlayerEntity.class, "serverAiStep");
//            try {
//                serverAiStepMethod.invoke(player);
//            } catch(IllegalAccessException e) {
//                throw new RuntimeException(e);
//            } catch(InvocationTargetException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        player.travel(new Vector3d((double)player.xxa, (double)player.yya, (double)player.zza));

        if (player.abilities.flying && !player.isPassenger()) {
            wasFlying = true;

            double d5 = player.getDeltaMovement().y;
            float f = player.flyingSpeed;
            player.flyingSpeed = player.abilities.getFlyingSpeed() * (float)(player.isSprinting() ? 2 : 1);





            if(player.isEffectiveAi() || player.isControlledByLocalInstance()) {
                BlockPos blockpos = new BlockPos(player.position().x, player.getBoundingBox().minY - 0.5000001D, player.position().z);
                float f3 = player.level.getBlockState(blockpos).getSlipperiness(player.level, blockpos, player);
                float f4 = player.isOnGround() ? f3 * 0.91F : 0.91F;
                Vector3d vector3d5 = player.handleRelativeFrictionAndCalculateMovement(new Vector3d((double)player.xxa, (double)player.yya, (double)player.zza), f3);
                double d2 = vector3d5.y;
                if (player.hasEffect(Effects.LEVITATION)) {
                    d2 += (0.05D * (double)(player.getEffect(Effects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    player.fallDistance = 0.0F;
                } else if (player.level.isClientSide && !player.level.hasChunkAt(blockpos)) {
                    if (player.getY() > 0.0D) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                } else if (!player.isNoGravity()) {
                    d2 -= 0.08;
                }

//            player.setDeltaMovement(vector3d5.x * (double)f4, d2 * (double)0.98F, vector3d5.z * (double)f4);
                player.setDeltaMovement(vector3d5.x * (double)f4, d2, vector3d5.z * (double)f4);






//            player.move(MoverType.SELF, player.getDeltaMovement());
//            player.setDeltaMovement(player.getDeltaMovement().add(0, -0.08, 0));
            }







            Vector3d vector3d = player.getDeltaMovement();
            player.setDeltaMovement(vector3d.x, d5 * 0.6D, vector3d.z);
            player.flyingSpeed = f;
            player.fallDistance = 0.0F;
//            player.setSharedFlag(7, false);
        } else {
            if(wasFlying) {
                Vector3d d = player.getDeltaMovement();
                player.setDeltaMovement(d.x, -0.08, d.z);
            }

            wasFlying = false;

            if(player.isEffectiveAi() || player.isControlledByLocalInstance()) {
                BlockPos blockpos = new BlockPos(player.position().x, player.getBoundingBox().minY - 0.5000001D, player.position().z);
                float f3 = player.level.getBlockState(blockpos).getSlipperiness(player.level, blockpos, player);
                float f4 = player.isOnGround() ? f3 * 0.91F : 0.91F;
                Vector3d vector3d5 = player.handleRelativeFrictionAndCalculateMovement(new Vector3d((double)player.xxa, (double)player.yya, (double)player.zza), f3);
                double d2 = vector3d5.y;
                if (player.hasEffect(Effects.LEVITATION)) {
                    d2 += (0.05D * (double)(player.getEffect(Effects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    player.fallDistance = 0.0F;
                } else if (player.level.isClientSide && !player.level.hasChunkAt(blockpos)) {
                    if (player.getY() > 0.0D) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                } else if (!player.isNoGravity()) {
                    d2 -= 0.08;
                }

    //            player.setDeltaMovement(vector3d5.x * (double)f4, d2 * (double)0.98F, vector3d5.z * (double)f4);
                player.setDeltaMovement(vector3d5.x * (double)f4, d2, vector3d5.z * (double)f4);






    //            player.move(MoverType.SELF, player.getDeltaMovement());
    //            player.setDeltaMovement(player.getDeltaMovement().add(0, -0.08, 0));
            }
        }

//        player.abilities.flying = true;
//
//        if(player.isEffectiveAi() || player.isControlledByLocalInstance()) {
//            BlockPos blockpos = new BlockPos(player.position().x, player.getBoundingBox().minY - 0.5000001D, player.position().z);
//            float f3 = player.level.getBlockState(blockpos).getSlipperiness(player.level, blockpos, player);
//            float f4 = player.isOnGround() ? f3 * 0.91F : 0.91F;
//            Vector3d vector3d5 = player.handleRelativeFrictionAndCalculateMovement(new Vector3d((double)player.xxa, (double)player.yya, (double)player.zza), f3);
//            double d2 = vector3d5.y;
//            if (player.hasEffect(Effects.LEVITATION)) {
//                d2 += (0.05D * (double)(player.getEffect(Effects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
//                player.fallDistance = 0.0F;
//            } else if (player.level.isClientSide && !player.level.hasChunkAt(blockpos)) {
//                if (player.getY() > 0.0D) {
//                    d2 = -0.1D;
//                } else {
//                    d2 = 0.0D;
//                }
//            } else if (!player.isNoGravity()) {
//                d2 -= 0.08;
//            }
//
////            player.setDeltaMovement(vector3d5.x * (double)f4, d2 * (double)0.98F, vector3d5.z * (double)f4);
//            player.setDeltaMovement(vector3d5.x * (double)f4, d2, vector3d5.z * (double)f4);
//
//
//
//
//
//
////            player.move(MoverType.SELF, player.getDeltaMovement());
////            player.setDeltaMovement(player.getDeltaMovement().add(0, -0.08, 0));
//        }
    }
}