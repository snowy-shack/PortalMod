package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.PortalTab;
import io.github.serialsniper.portalmod.common.items.AntlineBlockItem;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.common.items.RadioBlockItem;
import io.github.serialsniper.portalmod.common.items.WrenchItem;
import net.minecraft.item.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.registries.*;

public class ItemInit {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortalMod.MODID);
	
	public static final RegistryObject<PortalGun> PORTALGUN = ITEMS.register("portalgun",
			() -> new PortalGun(new Item.Properties().stacksTo(1).tab(PortalTab.INSTANCE)));
	
	public static final RegistryObject<BlockItem> PORTALABLE_BLOCK = ITEMS.register("portalable_block",
			() -> new BlockItem(BlockInit.PORTALABLE_BLOCK.get(), new Item.Properties().tab(PortalTab.INSTANCE)));
	
	public static final RegistryObject<RadioBlockItem> RADIO = ITEMS.register("radio",
			() -> new RadioBlockItem(BlockInit.RADIO.get(), new Item.Properties().tab(PortalTab.INSTANCE)));

//	public static final RegistryObject<BlockItem> STENCILBOX = ITEMS.register("stencilbox",
//			() -> new BlockItem(BlockInit.STENCILBOX.get(), new Item.Properties().tab(PortalTab.INSTANCE)));

	public static final RegistryObject<AntlineBlockItem> ANTLINE = ITEMS.register("antline",
			() -> new AntlineBlockItem(BlockInit.ANTLINE.get(), new Item.Properties().tab(PortalTab.INSTANCE)));

	public static final RegistryObject<BlockItem> FAITHPLATE = ITEMS.register("faithplate",
			() -> new BlockItem(BlockInit.FAITHPLATE.get(), new Item.Properties().tab(PortalTab.INSTANCE)));

	public static final RegistryObject<WrenchItem> WRENCH = ITEMS.register("wrench",
			() -> new WrenchItem(new Item.Properties().stacksTo(1).tab(PortalTab.INSTANCE)));
}