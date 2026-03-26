package net.portalmod.common.sorted.gel.container;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

public class EmptyGelContainer extends Item {
    public EmptyGelContainer(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);
        boolean creative = player.abilities.instabuild;
        Direction gelSide = context.getClickedFace().getOpposite();

        BooleanProperty sideProperty = AbstractGelBlock.STATES.get(gelSide);

        // Can't pick up any gel
        if (!(clickedState.getBlock() instanceof AbstractGelBlock) || !clickedState.getValue(sideProperty)) {
            return ActionResultType.PASS;
        }

        context.getLevel().setBlockAndUpdate(clickedPos, AbstractGelBlock.removeSide(gelSide, clickedState));
        context.getLevel().playSound(player, clickedPos, SoundInit.GEL_COLLECT.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSlightSoundPitch());

        // Fill container with gel
        if (!creative) {
            ItemStack newContainer = new ItemStack(clickedState.getBlock().asItem());
            newContainer.setTag(context.getItemInHand().getTag());
            GelContainer.setAmount(newContainer, 1);
            player.setItemInHand(context.getHand(), newContainer);
        }

        return ActionResultType.SUCCESS;
    }
}