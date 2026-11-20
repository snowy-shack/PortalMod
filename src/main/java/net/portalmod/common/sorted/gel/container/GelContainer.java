package net.portalmod.common.sorted.gel.container;

import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class GelContainer extends BlockItem {
    private final int color;
    private static final int maxAmount = 16;

    public GelContainer(Block block, Properties properties, int color) {
        super(block, properties);
        this.color = color;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);
        boolean creative = player.getAbilities().instabuild;
        Direction gelSide = context.getClickedFace().getOpposite();

        int gelAmount = getAmount(itemStack);

        BooleanProperty sideProperty = AbstractGelBlock.STATES.get(gelSide);
        boolean pickUpGel = clickedState.getBlock().is(this.getBlock())
                && clickedState.getValue(sideProperty)
                && (gelAmount < maxAmount || creative);

        InteractionResult result;

        if (pickUpGel) {
            // Pick up block
            context.getLevel().setBlockAndUpdate(clickedPos, AbstractGelBlock.removeSide(gelSide, clickedState));
            context.getLevel().playSound(player, clickedPos, SoundInit.GEL_COLLECT.get(), SoundSource.BLOCKS, 1, ModUtil.randomSlightSoundPitch());

            if (!creative) {
                increaseAmount(itemStack);
            }

            result = InteractionResult.SUCCESS;
        } else {
            // Place block
            result = super.useOn(context);
        }

        // Replace with empty container if empty
        if (getAmount(player.getItemInHand(context.getHand())) <= 0) {
            player.setItemInHand(
                    context.getHand(),
                    createEmptyContainer(itemStack)
            );
        }

        return result;
    }

    public static ItemStack createEmptyContainer(ItemStack stack) {
        ItemStack test = new ItemStack(ItemInit.CONTAINER.get());
        test.setTag(stack.getTag());
        return test;
    }

    public static void increaseAmount(ItemStack stack) {
        int amount = getAmount(stack);
        if (amount < maxAmount) {
            setAmount(stack, amount + 1);
        }
    }
    public static void decreaseAmount(ItemStack stack) {
        int amount = getAmount(stack);
        if (amount > 0) {
            setAmount(stack, amount - 1);
        }
    }

    public static int getAmount(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if(nbt.contains("amount"))
            return Math.max(nbt.getInt("amount"), 0);
        return maxAmount;
    }
    
    public static void setAmount(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("amount", Math.min(amount, maxAmount));
    }
    
    @Override
    protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        return SoundInit.GEL_PLACE.get();
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getAmount(stack) < maxAmount;
    }
    
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1f - (double) getAmount(stack) / maxAmount;
    }
    
    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return color == 0 ? super.getRGBDurabilityForDisplay(stack) : color;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip(this.getBlock().getRegistryName().getPath(), list);
    }
}