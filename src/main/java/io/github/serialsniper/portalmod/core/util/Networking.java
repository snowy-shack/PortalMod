package io.github.serialsniper.portalmod.core.util;

import io.github.serialsniper.portalmod.PortalMod;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.network.*;
import net.minecraftforge.fml.network.simple.*;

public class Networking {
	private static SimpleChannel INSTANCE;
	private static int ID = 0;
	
	private static int nextID() {
		return ID++;
	}
	
	public static void registerMessages() {
		INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(PortalMod.MODID + "networking"), () -> "1.0", s -> true, s -> true);
		INSTANCE.messageBuilder(PortalShootPacket.class, nextID())
			.encoder(PortalShootPacket::toBytes)
			.decoder(PortalShootPacket::new)
			.consumer(PortalShootPacket::handle)
			.add();
	}
	
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}
	
	public static void sendToClient(Object packet, ServerPlayerEntity player) {
		INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}