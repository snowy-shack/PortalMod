package net.portalmod.common.sorted.sign;

import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class ChamberSignItem extends Item {

    public ChamberSignItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos blockpos1 = blockpos.relative(direction);
        Player playerentity = context.getPlayer();
        ItemStack itemstack = context.getItemInHand();

        if (playerentity != null && !this.mayPlace(playerentity, direction, itemstack, blockpos1)) {
            return InteractionResult.PASS;
        }

        Level world = context.getLevel();

        boolean clickedBottomHalf = (context.getClickLocation().y % 1) < 0.25;
        boolean clickedTopHalf = (context.getClickLocation().y % 1) > 0.75;

        BlockPos placePos = clickedBottomHalf ? blockpos1.below() : blockpos1;
        ChamberSignEntity chamberSign = new ChamberSignEntity(world, placePos, direction, !clickedBottomHalf && !clickedTopHalf);

        CompoundTag compoundnbt = itemstack.getTag();
        if (compoundnbt != null) {
            EntityType.updateCustomEntityTag(world, playerentity, chamberSign, compoundnbt);
        }

        if (chamberSign.survives()) {
            if (!world.isClientSide) {
                chamberSign.playPlacementSound();
                world.addFreshEntity(chamberSign);
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos pos) {
        return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("chamber_sign", list);
    }
}
