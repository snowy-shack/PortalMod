package net.portalmod.common.sorted.portal;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager.LoadingValidationCallback;
import net.minecraftforge.common.world.ForgeChunkManager.TicketHelper;

public class PortalChunkLoader implements LoadingValidationCallback {
    @Override
    public void validateTickets(ServerWorld level, TicketHelper ticketHelper) {}
}