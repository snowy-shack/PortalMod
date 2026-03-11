package net.portalmod.common.sorted.portal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.core.math.Vec3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortalServerProofManager {
    private static PortalServerProofManager instance;

    private final Map<ServerPlayerEntity, BlockInteractionProof> blockInteractionProofs;

    private PortalServerProofManager() {
        this.blockInteractionProofs = new HashMap<>();
    }

    public static PortalServerProofManager getInstance() {
        if(instance == null)
            instance = new PortalServerProofManager();
        return instance;
    }

    public void setProof(ServerPlayerEntity player, int tick, int[] portalChain) {
        this.blockInteractionProofs.put(player, new BlockInteractionProof(tick, portalChain));
    }

    public boolean hasBelievableProof(ServerPlayerEntity player, BlockPos pos, boolean breakBlock) {
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

        public boolean isValid(ServerPlayerEntity player, BlockPos pos, boolean breakBlock) {
            MinecraftServer server = player.getServer();
            ServerWorld level = player.getLevel();

            if(server == null || server.getTickCount() != this.tick)
                return false;

            ModifiableAttributeInstance attribute = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get());
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