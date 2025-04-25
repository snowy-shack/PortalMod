package net.portalmod.common.sorted.sign;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class ChamberSignItem extends Item {

    public ChamberSignItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos blockpos1 = blockpos.relative(direction);
        PlayerEntity playerentity = context.getPlayer();
        ItemStack itemstack = context.getItemInHand();

        if (playerentity != null && !this.mayPlace(playerentity, direction, itemstack, blockpos1)) {
            return ActionResultType.PASS;
        }

        World world = context.getLevel();
        ChamberSignEntity chamberSign = new ChamberSignEntity(world, blockpos1, direction);

        CompoundNBT compoundnbt = itemstack.getTag();
        if (compoundnbt != null) {
            EntityType.updateCustomEntityTag(world, playerentity, chamberSign, compoundnbt);
        }

        if (chamberSign.survives()) {
            if (!world.isClientSide) {
                chamberSign.playPlacementSound();
                world.addFreshEntity(chamberSign);
            }

            itemstack.shrink(1);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        return ActionResultType.PASS;
    }

    protected boolean mayPlace(PlayerEntity player, Direction direction, ItemStack itemStack, BlockPos pos) {
        return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("chamber_sign", list);
    }
}
