package net.portalmod.common.sorted.gel.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.util.ModUtil;

public class EmptyGelContainer extends Item {
    public EmptyGelContainer(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemUseContext context = new ItemUseContext(player, hand, ModUtil.rayTraceBlock(player, level, 10));
        
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        if(block instanceof AbstractGelBlock) {
            stack = new ItemStack(((AbstractGelBlock)block).asItem());
            GelContainer.setAmount(stack, 1);
            
            Direction face = context.getClickedFace().getOpposite();
            BlockState previousState = level.getBlockState(pos);
            BlockState newState = previousState.setValue(AbstractGelBlock.STATES.get(face), false);
            
            boolean emptyState = true;
            for(Direction facing : Direction.values())
                if(newState.getValue(AbstractGelBlock.STATES.get(facing)))
                    emptyState = false;
            if(emptyState)
                newState = Blocks.AIR.defaultBlockState();
            
            level.levelEvent(player, 2001, pos, Block.getId(state));
            level.setBlock(pos, newState, BlockFlags.DEFAULT);
            return ActionResult.success(stack);
        }
        
        return ActionResult.fail(stack);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return ActionResultType.PASS;
    }
}