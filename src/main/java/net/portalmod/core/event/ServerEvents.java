package net.portalmod.core.event;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.injectors.LivingEntityInjector;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent event) {
//        PlayerEntity player = event.player;
//
//        if(event.phase == Phase.START)
//            if(player.abilities.flying)
//                ((IFaithPlateLaunchable)player).setLaunched(false);
//
//        if(event.phase == Phase.END)
//            if(player.inventory.getSelected().getItem() != ItemInit.WRENCH.get())
//                FaithPlateTER.selected = null;
//
////        if(player.abilities.flying && !player.isPassenger()) {
////            Vector3d velocity = player.getDeltaMovement().multiply(1, 1. / .6, 1);
////            if(velocity.y > 10)
////                velocity = new Vector3d(velocity.x, 10, velocity.z);
////            player.setDeltaMovement(velocity);
////        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(final LivingUpdateEvent event) {
        LivingEntityInjector.onPreTick(event.getEntityLiving());

        LivingEntity entity = (LivingEntity)event.getEntity();
        World level = entity.level;
        BlockPos onPos;
        
        {
            int i = MathHelper.floor(entity.position().x);
            int j = MathHelper.floor(entity.position().y - (double)0.2F);
            int k = MathHelper.floor(entity.position().z);
            onPos = new BlockPos(i, j, k);
            if(entity.level.isEmptyBlock(onPos)) {
                BlockPos blockpos1 = onPos.below();
                BlockState blockstate = entity.level.getBlockState(blockpos1);
                if(blockstate.collisionExtendsVertically(entity.level, blockpos1, entity))
                    onPos = blockpos1;
            }
        }
        
        boolean isCollidingFaithPlate = false;
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        BlockPos blockpos = new BlockPos(axisalignedbb.minX - .5, axisalignedbb.minY - .5, axisalignedbb.minZ - .5);
        BlockPos blockpos1 = new BlockPos(axisalignedbb.maxX + .5, axisalignedbb.maxY + .5, axisalignedbb.maxZ + .5);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        
        // TODO make method willLaunch in faithplate
        cycle:
        if(level.hasChunksAt(blockpos, blockpos1)) {
           for(int i = blockpos.getX(); i <= blockpos1.getX(); i++) {
              for(int j = blockpos.getY(); j <= blockpos1.getY(); j++) {
                 for(int k = blockpos.getZ(); k <= blockpos1.getZ(); k++) {
                    blockpos$mutable.set(i, j, k);
                    TileEntity blockEntity = level.getBlockEntity(blockpos$mutable);
                    if(blockEntity instanceof FaithPlateTileEntity) {
                        FaithPlateTileEntity faithPlate = (FaithPlateTileEntity)blockEntity;
                        if(((FaithPlateTileEntity)blockEntity).getTrigger().intersects(axisalignedbb)
                            && !(faithPlate.getTargetPos() == null || faithPlate.getTargetFace() == null || !faithPlate.isEnabled())) {
                            isCollidingFaithPlate = true;
                            break cycle;
                        }
                    }
                 }
              }
           }
        }
        
//        if((entity.horizontalCollision || entity.verticalCollision) && !isCollidingFaithPlate)
//            ((IFaithPlateLaunchable)entity).setLaunched(false);
    }
    
    @SubscribeEvent
    public static void onLivingFall(final LivingFallEvent event) {
        if(event.getEntityLiving().getItemBySlot(EquipmentSlotType.FEET).getItem() == ItemInit.LONGFALL_BOOTS.get())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent event) {
        // TODO add to present method
//        if(event.phase == TickEvent.Phase.END)
//            PortalFirstPersonRenderer.updateSwingTime();
    }
}