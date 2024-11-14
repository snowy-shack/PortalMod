package net.portalmod.common.sorted.gel.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import static net.portalmod.common.sorted.gel.AbstractGelBlock.STATES;

public class GelContainer extends BlockItem {
    private final int color;
    private static final int maxAmount = 16;

    public GelContainer(Block block, Properties properties, int color) {
        super(block, properties);
        this.color = color;
    }
    
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
//        if(level.isClientSide)
//            ActionResult.pass(null);
        final ItemStack emptyContainerStack = new ItemStack(ItemInit.CONTAINER.get());
        
        ItemStack stack = player.getItemInHand(hand);
        BlockRayTraceResult blockRayTraceResult = ModUtil.rayTraceBlock(player, level, 10);
        ItemUseContext context = new ItemUseContext(player, hand, blockRayTraceResult);

        Vector3d position = blockRayTraceResult.getLocation();

        double distance = position.subtract(player.getEyePosition(0)).length();
        if (distance > player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue()) return ActionResult.fail(stack); // Out of reach

        BlockItemUseContext itemContext = new BlockItemUseContext(context);
        
        BlockPos pos = context.getClickedPos();
        BlockPos relativePos = pos.relative(context.getClickedFace());
        BlockState state = level.getBlockState(pos);
        BlockState relativeState = level.getBlockState(relativePos);
        
        Block block = state.getBlock();
        Item item = stack.getItem();
        BlockItem blockItem = (BlockItem)item;
        
        if (!state.canBeReplaced(itemContext) && !relativeState.canBeReplaced(itemContext)) return ActionResult.fail(stack);
        
        if (block instanceof AbstractGelBlock) { // Collecting
            if (blockItem.getBlock() != block) return ActionResult.fail(stack);

            Direction face = context.getClickedFace().getOpposite();

            int newAmount = getAmount(stack) + 1;
            // (Overfull:                                     || Tried collecting a face that doesn't exist: )
            if (newAmount > maxAmount && !player.isCreative() || !state.getValue(STATES.get(face))) return ActionResult.fail(stack);

            setAmount(stack, newAmount);

            BlockState currentState = level.getBlockState(pos);
            BlockState newState = currentState.setValue(STATES.get(face), false);
            
            boolean stateIsEmpty = true;
            for (Direction facing : Direction.values())
                if (newState.getValue(STATES.get(facing))) stateIsEmpty = false;

            if (stateIsEmpty) newState = Blocks.AIR.defaultBlockState();

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundInit.GEL_COLLECT.get(), SoundCategory.BLOCKS, 1, 1);
            level.setBlock(pos, newState, BlockFlags.DEFAULT);

        } else { // Placing
            int currentAmount = getAmount(stack);

            Boolean wasSurvival = !player.isCreative();          // This is a terrible solution, but since BlockItem.place checks for instabuild,
            if (wasSurvival) player.abilities.instabuild = true; // this works as a temporary way to circumvent the item from being consumed.
            if (!super.place(itemContext).consumesAction()) return ActionResult.fail(stack);
            if (wasSurvival) player.abilities.instabuild = false;
            
            if (currentAmount <= 1) return ActionResult.success(emptyContainerStack);
            setAmount(stack, player.isCreative() ? currentAmount : currentAmount - 1);
        }
        return ActionResult.success(stack);
    }

//    @Override
//    public ActionResultType place(BlockItemUseContext context) {
//        if (!context.canPlace()) return ActionResultType.FAIL;
//
//        BlockItemUseContext blockItemUseContext = this.updatePlacementContext(context);
//        if (blockItemUseContext == null) return ActionResultType.FAIL;
//
//        BlockState newBlockState = this.getPlacementState(blockItemUseContext);
//        if (newBlockState == null) return ActionResultType.FAIL;
//        if (!this.placeBlock(blockItemUseContext, newBlockState)) return ActionResultType.FAIL;
//
//        BlockPos clickedPos = blockItemUseContext.getClickedPos();
//        World level = blockItemUseContext.getLevel();
//        PlayerEntity player = blockItemUseContext.getPlayer();
//        ItemStack itemInHand = blockItemUseContext.getItemInHand();
//        BlockState blockState = level.getBlockState(clickedPos);
//        Block block = blockState.getBlock();
//
//        if (block == newBlockState.getBlock()) {
//            blockState = this.updateBlockStateFromTag(clickedPos, level, itemInHand, blockState);
//            this.updateCustomBlockEntityTag(clickedPos, level, player, itemInHand, blockState);
//            block.setPlacedBy(level, clickedPos, blockState, player, itemInHand);
//
//            if (player instanceof ServerPlayerEntity) {
//                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity)player, clickedPos, itemInHand);
//            }
//        }
//
//        SoundType soundtype = blockState.getSoundType(level, clickedPos, context.getPlayer());
//        level.playSound(player, clickedPos, this.getPlaceSound(blockState, level, clickedPos, context.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
//
//        return ActionResultType.sidedSuccess(level.isClientSide);
//    }

    public static int getAmount(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if(nbt.contains("amount"))
            return nbt.getInt("amount");
        return maxAmount;
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