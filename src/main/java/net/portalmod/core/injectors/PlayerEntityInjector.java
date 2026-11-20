package net.portalmod.core.injectors;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.stats.Stats;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PlayerEntityInjector {
    static boolean wasFlying = false;

    public static void travel(Player player) {
//        Vec3 delta = PortalEntity.teleportEntity(player, player.getDeltaMovement());
//        player.setBoundingBox(player.getBoundingBox().move(delta));
//        player.setLocationFromBoundingbox();
//        if(player.level.isClientSide) {
//            Method serverAiStepMethod = ObfuscationReflectionHelper.findMethod(LocalPlayer.class, "serverAiStep");
//            try {
//                serverAiStepMethod.invoke(player);
//            } catch(IllegalAccessException e) {
//                throw new RuntimeException(e);
//            } catch(InvocationTargetException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        player.travel(new Vec3((double)player.xxa, (double)player.yya, (double)player.zza));

        if (player.getAbilities().flying && !player.isPassenger()) {
            wasFlying = true;

            double d5 = player.getDeltaMovement().y;
            // 1.16 temporarily set player.flyingSpeed here; in 1.20.1 Player#getFlyingSpeed already
            // returns the abilities speed (doubled when sprinting), so nothing to set.





            if(player.isEffectiveAi() || player.isControlledByLocalInstance()) {
                BlockPos blockpos = BlockPos.containing(player.position().x, player.getBoundingBox().minY - 0.5000001D, player.position().z);
                float f3 = player.level().getBlockState(blockpos).getBlock().getFriction();
                float f4 = player.onGround() ? f3 * 0.91F : 0.91F;
                Vec3 vector3d5 = player.handleRelativeFrictionAndCalculateMovement(new Vec3((double)player.xxa, (double)player.yya, (double)player.zza), f3);
                double d2 = vector3d5.y;
                if (player.hasEffect(MobEffects.LEVITATION)) {
                    d2 += (0.05D * (double)(player.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    player.fallDistance = 0.0F;
                } else if (player.level().isClientSide && !player.level().hasChunkAt(blockpos)) {
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







            Vec3 vector3d = player.getDeltaMovement();
            player.setDeltaMovement(vector3d.x, d5 * 0.6D, vector3d.z);
            player.fallDistance = 0.0F;
//            player.setSharedFlag(7, false);
        } else {
            if(wasFlying) {
                Vec3 d = player.getDeltaMovement();
                player.setDeltaMovement(d.x, -0.08, d.z);
            }

            wasFlying = false;

            if(player.isEffectiveAi() || player.isControlledByLocalInstance()) {
                BlockPos blockpos = BlockPos.containing(player.position().x, player.getBoundingBox().minY - 0.5000001D, player.position().z);
                float f3 = player.level().getBlockState(blockpos).getBlock().getFriction();
                float f4 = player.onGround() ? f3 * 0.91F : 0.91F;
                Vec3 vector3d5 = player.handleRelativeFrictionAndCalculateMovement(new Vec3((double)player.xxa, (double)player.yya, (double)player.zza), f3);
                double d2 = vector3d5.y;
                if (player.hasEffect(MobEffects.LEVITATION)) {
                    d2 += (0.05D * (double)(player.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    player.fallDistance = 0.0F;
                } else if (player.level().isClientSide && !player.level().hasChunkAt(blockpos)) {
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
//            float f4 = player.onGround() ? f3 * 0.91F : 0.91F;
//            Vec3 vector3d5 = player.handleRelativeFrictionAndCalculateMovement(new Vec3((double)player.xxa, (double)player.yya, (double)player.zza), f3);
//            double d2 = vector3d5.y;
//            if (player.hasEffect(MobEffects.LEVITATION)) {
//                d2 += (0.05D * (double)(player.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
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