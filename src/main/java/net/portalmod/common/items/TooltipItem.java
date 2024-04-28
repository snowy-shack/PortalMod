package net.portalmod.common.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class TooltipItem extends Item {

    public final String tooltip;

    public TooltipItem(Properties properties, String tooltip) {
        super(properties);
        this.tooltip = tooltip;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World p_77624_2_, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip(this.tooltip, list);
    }
}
