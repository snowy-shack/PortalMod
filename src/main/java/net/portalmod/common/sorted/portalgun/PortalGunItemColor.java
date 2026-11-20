package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class PortalGunItemColor implements ItemColor {
    private static final int BLUE = 0x3B8BFF;
    private static final int ORANGE = 0xFF833B;

    // TODO sync with gun
    @Override
    public int getColor(ItemStack itemStack, int layer) {
        if(layer != 0 || !itemStack.hasTag())
            return -1;

        CompoundTag nbt = itemStack.getTag();
        if(!nbt.contains("color"))
            return -1;

        byte color = nbt.getByte("color");
        if(color == 1)
            return BLUE;
        if(color == 2)
            return ORANGE;
        return -1;
    }
}