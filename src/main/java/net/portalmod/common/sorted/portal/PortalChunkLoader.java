package net.portalmod.common.sorted.portal;

import net.minecraftforge.common.world.ForgeChunkManager.LoadingValidationCallback;
import net.minecraftforge.common.world.ForgeChunkManager.TicketHelper;

import net.minecraft.server.level.ServerLevel;

public class PortalChunkLoader implements LoadingValidationCallback {
    @Override
    public void validateTickets(ServerLevel level, TicketHelper ticketHelper) {}
}