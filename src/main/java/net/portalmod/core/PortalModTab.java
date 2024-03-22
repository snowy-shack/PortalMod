package net.portalmod.core;

import net.minecraft.item.*;
import net.portalmod.PortalMod;
import net.portalmod.core.init.ItemInit;

public class PortalModTab extends ItemGroup {
    public static final ItemGroup INSTANCE = new PortalModTab();
    
    private PortalModTab() {
        super(PortalMod.MODID);
    }
    
    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ItemInit.RADIO.get());
    }
}