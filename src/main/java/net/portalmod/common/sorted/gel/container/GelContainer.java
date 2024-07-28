package net.portalmod.common.sorted.gel.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

public class GelContainer extends BlockItem {
    private final int color;
    
    public GelContainer(Block block, Properties properties, int color) {
        super(block, properties);
        this.color = color;
    }
    
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
//        if(level.isClientSide)
//            ActionResult.pass(null);
        
        final ItemStack empty = new ItemStack(ItemInit.CONTAINER.get());
        
        ItemStack stack = player.getItemInHand(hand);
        ItemUseContext context = new ItemUseContext(player, hand, ModUtil.rayTraceBlock(player, level, 10));
        BlockItemUseContext itemContext = new BlockItemUseContext(context);
        
        BlockPos pos = context.getClickedPos();
        BlockPos relativePos = pos.relative(context.getClickedFace());
        BlockState state = level.getBlockState(pos);
        BlockState relativeState = level.getBlockState(relativePos);
        
        Block block = state.getBlock();
        Item item = stack.getItem();
        BlockItem blockItem = (BlockItem)item;
        
        if(!(state.canBeReplaced(itemContext) || relativeState.canBeReplaced(itemContext)))
            return ActionResult.fail(stack);
        
        if(block instanceof AbstractGelBlock) {
            if(blockItem.getBlock() != block)
                return ActionResult.fail(stack);
            
            int amount = getAmount(stack) + 1;
            if(amount > 16)
                return ActionResult.fail(stack);
            if(amount <= 0)
                return ActionResult.fail(new ItemStack(ItemInit.CONTAINER.get()));
            setAmount(stack, amount);
            
            Direction face = context.getClickedFace().getOpposite();
            BlockState previousState = level.getBlockState(pos);
            BlockState newState = previousState.setValue(AbstractGelBlock.STATES.get(face), false);
            
            boolean emptyState = true;
            for(Direction facing : Direction.values())
                if(newState.getValue(AbstractGelBlock.STATES.get(facing)))
                    emptyState = false;
            if(emptyState)
                newState = Blocks.AIR.defaultBlockState();

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundInit.GEL_COLLECT.get(), SoundCategory.BLOCKS, 1, 1);

//            level.levelEvent(player, 2001, pos, Block.getId(state)); // What is this for??
            level.setBlock(pos, newState, BlockFlags.DEFAULT);
            
        } else {
            int amount = getAmount(stack);
            if(amount <= 0)
                return ActionResult.fail(empty);
            if(amount > 16) {
                amount = 16;
                setAmount(stack, 16);
            }
            
            if(!super.place(itemContext).consumesAction())
                return ActionResult.fail(stack);
            
            if(--amount <= 0)
                return ActionResult.success(empty);
            setAmount(stack, amount);
        }

        return ActionResult.success(stack);
    }
    
    public static int getAmount(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if(nbt.contains("amount"))
            return nbt.getInt("amount");
        return 16;
    }
    
    public static void setAmount(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("amount", amount);
    }
    
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return ActionResultType.PASS;
    }
    
    @Override
    protected SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
        return SoundInit.GEL_PLACE.get();
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getAmount(stack) < 16;
    }
    
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1f - getAmount(stack) / 16f;
    }
    
    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return color == 0 ? super.getRGBDurabilityForDisplay(stack) : color;
    }
}