package net.portalmod.common.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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

import javax.annotation.Nonnull;
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
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        BlockRayTraceResult rayHit = ModUtil.rayTraceBlock(player, world, 64);
        Direction clickFace = rayHit.getDirection();
        Vector3d clickPos = rayHit.getLocation();
        ItemStack itemStack = player.getItemInHand(hand);

        if(!world.isClientSide) return super.use(world, player, hand);

        if(FaithPlateTER.selected != null) {
            BlockPos selected = FaithPlateTER.selected;
            TileEntity blockEntity = world.getBlockEntity(selected);

            if (!(blockEntity instanceof FaithPlateTileEntity)) {
                return ActionResult.fail(itemStack);
            }
            FaithPlateTileEntity be = (FaithPlateTileEntity) blockEntity;

            // Round the target pos to half blocks
            clickPos = getTargetPos(clickFace, clickPos);

            boolean enabled = false;
            // Set the default height to dist / n
            if (be.getTargetPos() == null) {
                be.setHeight(be.getPredictedHeight(clickPos));
                enabled = true;
            }

            CompoundNBT nbt = new CompoundNBT();
            CompoundNBT target = new CompoundNBT();
            target.putFloat("height", be.getHeight());
            nbt.putBoolean("enabled", enabled || be.isEnabled());

            target.putByte("side", (byte) clickFace.get3DDataValue());
            target.putDouble("x", clickPos.x() - selected.getX());
            target.putDouble("y", clickPos.y() - selected.getY());
            target.putDouble("z", clickPos.z() - selected.getZ());

            nbt.put("target", target);
            be.load(nbt);

            PacketInit.INSTANCE.sendToServer(new CFaithPlateUpdatedPacket(selected, nbt));
            PacketInit.INSTANCE.sendToServer(new CFaithPlateEndConfigPacket(selected));
            FaithPlateTER.selected = null;

            WrenchItem.playUseSound(world, player.position());
            return ActionResult.success(itemStack);
        }

        if (TriggerSelectionClient.isSelecting()) {
            WrenchItem.playUseSound(player, world, player.position());

            TriggerSelectionClient.confirmSelection();

            return ActionResult.success(itemStack);
        }

        return ActionResult.fail(itemStack);
    }

    @Nonnull
    public static Vector3d getTargetPos(Direction clickFace, Vector3d clickPos) {
        clickPos = new Vector3d(
                Math.round(clickPos.x() * 2) / 2.0,
                Math.round(clickPos.y() * 2) / 2.0,
                Math.round(clickPos.z() * 2) / 2.0
        ).subtract(
                0.5 + clickFace.getStepX() * 0.5,
                0.5 + clickFace.getStepY() * 0.5,
                0.5 + clickFace.getStepZ() * 0.5
        );
        return clickPos;
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