package io.github.serialsniper.portalmod.common;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import net.minecraft.item.*;

public class PortalTab extends ItemGroup {
    public static final ItemGroup INSTANCE = new PortalTab();

    public PortalTab() {
        super(PortalMod.MODID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ItemInit.RADIO.get());
    }
}