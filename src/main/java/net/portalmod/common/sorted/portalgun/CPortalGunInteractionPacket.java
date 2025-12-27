package net.portalmod.common.sorted.portalgun;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.button.StandingButtonBlock;
import net.portalmod.common.sorted.portal.ITeleportable;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.init.CriteriaTriggerInit;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CPortalGunInteractionPacket implements AbstractPacket<CPortalGunInteractionPacket> {
    private PortalGunInteraction type;
    private PortalEnd end;
    private int data;
    private BlockRayTraceResult blockHit;

    public CPortalGunInteractionPacket() {}

    public CPortalGunInteractionPacket(PortalGunInteraction type, PortalEnd end, int data, BlockRayTraceResult blockHit) {
        this.type = type;
        this.end = end;
        this.data = data;
        this.blockHit = blockHit;
    }

    public static class Builder {
        private final PortalGunInteraction type;
        private PortalEnd end = PortalEnd.NONE;
        private int data = -1;
        private BlockRayTraceResult blockHit = BlockRayTraceResult.miss(Vector3d.ZERO, Direction.NORTH, BlockPos.ZERO);
        
        public Builder(PortalGunInteraction type) {
            this.type = type;
        }
        
        public Builder end(PortalEnd end) {
            this.end = end;
            return this;
        }
        
        public Builder data(int data) {
            this.data = data;
            return this;
        }

        public Builder blockHit(BlockRayTraceResult blockHit) {
            this.blockHit = blockHit;
            return this;
        }
        
        public CPortalGunInteractionPacket build() {
            return new CPortalGunInteractionPacket(type, end, data, blockHit);
        }
    }
    
    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
        buffer.writeEnum(end);
        buffer.writeInt(data);
        buffer.writeBlockHitResult(blockHit);
    }

    @Override
    public CPortalGunInteractionPacket decode(PacketBuffer buffer) {
        return new CPortalGunInteractionPacket(
                buffer.readEnum(PortalGunInteraction.class),
                buffer.readEnum(PortalEnd.class),
                buffer.readInt(),
                buffer.readBlockHitResult());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player == null)
                return;

            switch(type) {
                case PICK_ENTITY:
                    CriteriaTriggerInit.GRAB_ENTITY.get().trigger(player);
                    Entity entity = player.level.getEntity(data);

                    if (entity instanceof TestElementEntity) {
                        ((TestElementEntity) entity).pickUp(player);
                    }

                    // todo only temporary fix
                    ((ITeleportable) entity).removeLastUsedPortal();

                    break;

                case DROP_ENTITY:
                    TestElementEntity.dropHeldEntities(player, false, player.getMainHandItem());
                    break;

                case THROW_ENTITY:
                    TestElementEntity.dropHeldEntities(player, true, player.getMainHandItem());
                    break;

                case SHOOT_PORTAL:
                    PortalGun.placePortal(player, player.level, end, player.getMainHandItem());
                    break;

                case PRESS_BUTTON:
                    BlockState blockState = player.level.getBlockState(blockHit.getBlockPos());
                    Block block = blockState.getBlock();
                    if (block instanceof StandingButtonBlock && ((StandingButtonBlock) block).canActivate(blockState)) {
                        ((StandingButtonBlock) block).activate(blockState, player.level, blockHit.getBlockPos());
                    }
            }
        });

        context.get().setPacketHandled(true);
        return true;
    }
}