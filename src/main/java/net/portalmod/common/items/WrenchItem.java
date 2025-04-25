package net.portalmod.common.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.portalmod.common.sorted.faithplate.CFaithPlateUpdatedPacket;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.StreamSupport;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    public static void playUseSound(World world, Vector3d location) {
        world.playSound(null, location.x, location.y, location.z, SoundInit.WRENCH_USE.get(), SoundCategory.PLAYERS, 1f, ModUtil.randomSoundPitch());
    }

    public static boolean holdingWrench(Entity entity) {
        return StreamSupport.stream(entity.getHandSlots().spliterator(), false).anyMatch(itemStack -> itemStack.getItem() instanceof WrenchItem);
    }

    public static boolean usedWrench(LivingEntity entity, Hand hand) {
        return entity.getItemInHand(hand).getItem() instanceof WrenchItem;
    }

    public static boolean hitWithWrench(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof WrenchItem;
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        BlockPos pos = new BlockPos(6, 55, 20);
        Direction face = Direction.UP;
        BlockPos selected = new BlockPos(-5, 55, 20);
        TileEntity blockEntity = level.getBlockEntity(selected);
        ItemStack itemStack = player.getItemInHand(hand);

        // my bad this was naive, we need to detect if you are choosing a target at the moment and idk how yet :)
        if (!(blockEntity instanceof FaithPlateTileEntity)) {
            return ActionResult.fail(itemStack);
        }

        FaithPlateTileEntity be = (FaithPlateTileEntity) blockEntity;

        CompoundNBT nbt = new CompoundNBT();
        CompoundNBT target = new CompoundNBT();
        target.putInt("x", pos.getX() - selected.getX());
        target.putInt("y", pos.getY() - selected.getY());
        target.putInt("z", pos.getZ() - selected.getZ());
        target.putByte("side", (byte)face.get3DDataValue());
        target.putFloat("height", be.getHeight());
//            System.out.println(be.getHeight());
        nbt.put("target", target);

        nbt.putBoolean("enabled", true);
        be.load(nbt);

        new Vector3d(0, 0, 0).zRot(0);

        PacketInit.INSTANCE.sendToServer(new CFaithPlateUpdatedPacket(selected, nbt));
        FaithPlateTER.selected = null;
//        if(true)
        return ActionResult.success(itemStack);





//        BlockRayTraceResult rayHit = ModUtil.rayTraceBlock(player, level, 64);
//        Direction face = rayHit.getDirection();
//        BlockPos pos = rayHit.getBlockPos();
//
//        if(level.isClientSide && FaithPlateTER.selected != null) {
//            FaithPlateTileEntity be = (FaithPlateTileEntity)level.getBlockEntity(FaithPlateTER.selected);
//
//            CompoundNBT nbt = new CompoundNBT();
//            CompoundNBT target = new CompoundNBT();
//            target.putInt("x", pos.getX() - FaithPlateTER.selected.getX());
//            target.putInt("y", pos.getY() - FaithPlateTER.selected.getY());
//            target.putInt("z", pos.getZ() - FaithPlateTER.selected.getZ());
//            target.putByte("side", (byte)face.get3DDataValue());
//            target.putFloat("height", be.getHeight());
////            System.out.println(be.getHeight());
//            nbt.put("target", target);
//
//            nbt.putBoolean("enabled", true);
//            be.load(nbt);
//
//            PacketInit.INSTANCE.sendToServer(new CFaithPlateUpdatedPacket(FaithPlateTER.selected, nbt));
//            FaithPlateTER.selected = null;
//            return ActionResult.success(player.getItemInHand(hand));
//        }
//
//        return super.use(level, player, hand);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("wrench", list);
    }
}