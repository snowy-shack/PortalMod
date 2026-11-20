package net.portalmod.common.sorted.portal;

import net.minecraft.server.MinecraftServer;
import net.portalmod.core.math.Vec3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class PortalServerProofManager {
    private static PortalServerProofManager instance;

    private final Map<ServerPlayer, BlockInteractionProof> blockInteractionProofs;

    private PortalServerProofManager() {
        this.blockInteractionProofs = new HashMap<>();
    }

    public static PortalServerProofManager getInstance() {
        if(instance == null)
            instance = new PortalServerProofManager();
        return instance;
    }

    public void setProof(ServerPlayer player, int tick, int[] portalChain) {
        this.blockInteractionProofs.put(player, new BlockInteractionProof(tick, portalChain));
    }

    public boolean hasBelievableProof(ServerPlayer player, BlockPos pos, boolean breakBlock) {
        if(!this.blockInteractionProofs.containsKey(player))
            return false;
        return this.blockInteractionProofs.get(player).isValid(player, pos, breakBlock);
    }

    private static class BlockInteractionProof {
        private final int tick;
        private final int[] portalChain;

        public BlockInteractionProof(int tick, int[] portalChain) {
            this.tick = tick;
            this.portalChain = portalChain;
        }

        public boolean isValid(ServerPlayer player, BlockPos pos, boolean breakBlock) {
            MinecraftServer server = player.getServer();
            ServerLevel level = player.getLevel();

            if(server == null || server.getTickCount() != this.tick)
                return false;

            AttributeInstance attribute = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get());
            if(attribute == null)
                return false;

            List<Entity> entities = Arrays.stream(this.portalChain).mapToObj(level::getEntity).collect(Collectors.toList());
            double reach = attribute.getValue() + (breakBlock ? 1 : 3);
            reach *= reach;
            Vec3 blockPos = new Vec3(pos).add(0.5);
            Vec3 updatingPosition = new Vec3(player.position()).add(0, breakBlock ? 1.5 : 0, 0);

            for(Entity entity : entities) {
                if(!(entity instanceof PortalEntity))
                    return false;

                PortalEntity portal = (PortalEntity)entity;
                if(!portal.getOtherPortal().isPresent())
                    return false;

                double distance = updatingPosition.clone().sub(portal.position()).magnitudeSqr();
                if(distance > reach)
                    return false;

                updatingPosition = new Vec3(portal.getOtherPortal().get().position());
            }

            return updatingPosition.clone().sub(blockPos).magnitudeSqr() <= reach;
        }
    }
}