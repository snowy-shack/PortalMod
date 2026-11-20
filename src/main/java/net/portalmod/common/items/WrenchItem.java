package net.portalmod.common.items;

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

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    public static void playUseSound(Player player, Level world, Vec3 location) {
        world.playSound(player, location.x, location.y, location.z, SoundInit.WRENCH_USE.get(), SoundSource.PLAYERS, 1f, ModUtil.randomSoundPitch());
    }

    public static void playFailSound(Player player, Level world, Vec3 location) {
        world.playSound(player, location.x, location.y, location.z, SoundInit.WRENCH_FAIL.get(), SoundSource.PLAYERS, 1f, ModUtil.randomSoundPitch());
    }

    public static void playUseSound(Level world, Vec3 location) {
        playUseSound(null, world, location);
    }

    public static void playFailSound(Level world, Vec3 location) {
        playFailSound(null, world, location);
    }

    public static boolean holdingWrench(Entity entity) {
        return StreamSupport.stream(entity.getHandSlots().spliterator(), false).anyMatch(itemStack -> itemStack.getItem() instanceof WrenchItem);
    }

    public static boolean usedWrench(LivingEntity entity, InteractionHand hand) {
        return entity.getItemInHand(hand).getItem() instanceof WrenchItem;
    }

    public static boolean hitWithWrench(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof WrenchItem;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        BlockHitResult rayHit = ModUtil.rayTraceBlock(player, world, 64);
        Direction clickFace = rayHit.getDirection();
        Vec3 clickPos = rayHit.getLocation();
        ItemStack itemStack = player.getItemInHand(hand);

        if(!world.isClientSide) return super.use(world, player, hand);

        if(FaithPlateTER.selected != null) {
            BlockPos selected = FaithPlateTER.selected;
            BlockEntity blockEntity = world.getBlockEntity(selected);

            if (!(blockEntity instanceof FaithPlateTileEntity)) {
                return InteractionResultHolder.fail(itemStack);
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

            CompoundTag nbt = new CompoundTag();
            CompoundTag target = new CompoundTag();
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
            return InteractionResultHolder.success(itemStack);
        }

        if (TriggerSelectionClient.isSelecting()) {
            WrenchItem.playUseSound(player, world, player.position());

            TriggerSelectionClient.confirmSelection();

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Nonnull
    public static Vec3 getTargetPos(Direction clickFace, Vec3 clickPos) {
        clickPos = new Vec3(
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
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("wrench", list);
    }
}