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
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.portalmod.common.sorted.faithplate.CFaithPlateEndConfigPacket;
import net.portalmod.common.sorted.faithplate.CFaithPlateUpdatedPacket;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.trigger.TriggerSelectionClient;
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

    public static void playUseSound(PlayerEntity player, World world, Vector3d location) {
        world.playSound(player, location.x, location.y, location.z, SoundInit.WRENCH_USE.get(), SoundCategory.PLAYERS, 1f, ModUtil.randomSoundPitch());
    }

    public static void playFailSound(PlayerEntity player, World world, Vector3d location) {
        world.playSound(player, location.x, location.y, location.z, SoundInit.WRENCH_FAIL.get(), SoundCategory.PLAYERS, 1f, ModUtil.randomSoundPitch());
    }

    public static void playUseSound(World world, Vector3d location) {
        playUseSound(null, world, location);
    }

    public static void playFailSound(World world, Vector3d location) {
        playFailSound(null, world, location);
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
        BlockRayTraceResult rayHit = ModUtil.rayTraceBlock(player, level, 64);
        Direction face = rayHit.getDirection();
        BlockPos pos = rayHit.getBlockPos();
        ItemStack itemStack = player.getItemInHand(hand);

        if(level.isClientSide) {
            if(FaithPlateTER.selected != null) {
                BlockPos selected = FaithPlateTER.selected;
                TileEntity blockEntity = level.getBlockEntity(selected);

                if (!(blockEntity instanceof FaithPlateTileEntity)) {
                    return ActionResult.fail(itemStack);
                }
                FaithPlateTileEntity be = (FaithPlateTileEntity) blockEntity;

                // i have no idea why on earth this is needed but i wont touch it
                boolean enabled = false;
                // Set the default height to dist / n
                if (be.getTargetPos() == null) {
                    be.setHeight((float) (pos.distManhattan(selected) / 4.0));
                    enabled = true;
                }

                CompoundNBT nbt = new CompoundNBT();
                CompoundNBT target = new CompoundNBT();
                target.putFloat("height", be.getHeight());
                nbt.putBoolean("enabled", enabled || be.isEnabled());

                target.putByte("side", (byte) face.get3DDataValue());
                target.putInt("x", pos.getX() - selected.getX());
                target.putInt("y", pos.getY() - selected.getY());
                target.putInt("z", pos.getZ() - selected.getZ());

                nbt.put("target", target);
                be.load(nbt);

                PacketInit.INSTANCE.sendToServer(new CFaithPlateUpdatedPacket(selected, nbt));
                PacketInit.INSTANCE.sendToServer(new CFaithPlateEndConfigPacket(selected));
                FaithPlateTER.selected = null;

                return ActionResult.success(itemStack);
            }

            if (TriggerSelectionClient.isSelecting()) {
                WrenchItem.playUseSound(player, level, player.position());

                TriggerSelectionClient.confirmSelection();

                return ActionResult.success(itemStack);
            }
        }

        return super.use(level, player, hand);
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