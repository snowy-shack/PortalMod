package net.portalmod.common.sorted.gel.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;


public class GelContainer extends BlockItem {
    private final int color;
    private static final int maxAmount = 16;

    public GelContainer(Block block, Properties properties, int color) {
        super(block, properties);
        this.color = color;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ActionResult<ItemStack> original = super.use(world, player, hand);


        return original;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);
        boolean creative = player.abilities.instabuild;
        Direction gelSide = context.getClickedFace().getOpposite();

        int gelAmount = getAmount(itemStack);

        BooleanProperty sideProperty = AbstractGelBlock.STATES.get(gelSide);
        boolean pickUpGel = clickedState.getBlock().is(this.getBlock())
                && clickedState.getValue(sideProperty)
                && (gelAmount < maxAmount || creative);

        ActionResultType result;

        if (pickUpGel) {
            // Pick up block
            context.getLevel().setBlockAndUpdate(clickedPos, AbstractGelBlock.removeSide(gelSide, clickedState));
            context.getLevel().playSound(player, clickedPos, SoundInit.GEL_COLLECT.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSlightSoundPitch());

            if (!creative) {
                increaseAmount(itemStack);
            }

            result = ActionResultType.SUCCESS;
        } else {
            // Place block
            result = super.useOn(context);
        }

        // Replace with empty container if empty
        if (getAmount(player.getItemInHand(context.getHand())) <= 0) {
            player.setItemInHand(context.getHand(), new ItemStack(ItemInit.CONTAINER.get()));
        }

        return result;
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
        CompoundNBT nbt = stack.getOrCreateTag();
        if(nbt.contains("amount"))
            return Math.max(nbt.getInt("amount"), 0);
        return maxAmount;
    }
    
    public static void setAmount(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("amount", Math.min(amount, maxAmount));
    }
    
    @Override
    protected SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
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
}