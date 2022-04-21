package io.github.serialsniper.portalmod.core.util;

import java.util.function.*;

import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.client.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraftforge.fml.network.*;

public class PortalShootPacket {
	private PortalEnd end;
	
	public PortalShootPacket(PacketBuffer buf) {
		end = buf.readEnum(PortalEnd.class);
	}
	
	public PortalShootPacket(PortalEnd end) {
		this.end = end;
	}
	
	public void toBytes(PacketBuffer buf) {
		buf.writeEnum(end);
	}
	
	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			@SuppressWarnings("resource")
			ItemStack item = Minecraft.getInstance().player.getMainHandItem();
			
			if(item.getItem() instanceof PortalGun)
				((PortalGun)item.getItem()).placePortal(PortalEnd.BLUE, ctx.get().getSender().getLevel(), ctx.get().getSender());
		});
		return true;
	}
}