package net.portalmod.common.items;

import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class BulletsItem extends Item {

    public BulletsItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level p_77624_2_, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("bullets", list);
    }
}
