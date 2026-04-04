package net.portalmod.common.sorted.portalgun;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.autoportal.AutoPortalBlock;
import net.portalmod.common.sorted.panel.PortalHelper;
import net.portalmod.common.sorted.portal.AbstractPortalHelper;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalHelperRepresentation;
import net.portalmod.common.sorted.portal.VolatilePortalHelper;
import net.portalmod.core.math.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalHelperServerManager {
    private static PortalHelperServerManager instance;

    private static final int HELPER_DELAY = 15;
    private Map<ServerPlayerEntity, PortalHelperState> helpStates;

    private PortalHelperServerManager() {
        this.helpStates = new HashMap<>();
    }

    public static PortalHelperServerManager getInstance() {
        if(instance == null)
            instance = new PortalHelperServerManager();
        return instance;
    }

    public void tick() {
        this.helpStates.forEach((player, state) -> state.tick());
    }

    public boolean willBeHelped(ServerPlayerEntity player, UUID gun, PortalEnd end, BlockPos pos, Direction face, Direction horizontalDirection, World level) {
        PortalHelperState helperState = this.helpStates.get(player);
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Block frontBlock = level.getBlockState(pos.relative(face)).getBlock();

        if(helperState != null && helperState.lastHelpedHelper instanceof PortalHelperRepresentation) {
            if(helperState.nextHelp > 0 && !(frontBlock instanceof AutoPortalBlock)) {
                if(helperState.isBasicallySame(gun, end, level, pos, face)) {
                    if(block instanceof PortalHelper && ((PortalHelper) block).willHelpPortal(face, horizontalDirection, state, level)) {
                        return false;
                    }
                }
            }
        }

        return block instanceof PortalHelper && ((PortalHelper)block).willHelpPortal(face, horizontalDirection, state, level);
    }

    public boolean willBeHelped(ServerPlayerEntity player, UUID gun, PortalEnd end, Vec3 position, Direction face, VolatilePortalHelper helper) {
        PortalHelperState helperState = this.helpStates.get(player);

        if(helperState != null && helperState.lastHelpedHelper instanceof VolatilePortalHelper) {
            if(helperState.nextHelp > 0) {
                if(helperState.isBasicallySame(gun, end, helper)) {
                    return false;
                }
            }
        }

        return helper.willHelpPortal(position, face);
    }

    public void setHelped(ServerPlayerEntity player, UUID gun, PortalEnd end, BlockPos pos, Direction face) {
        this.helpStates.put(player, new PortalHelperState(gun, end, new PortalHelperRepresentation(pos, face), HELPER_DELAY));
    }

    public void setHelped(ServerPlayerEntity player, UUID gun, PortalEnd end, VolatilePortalHelper helper) {
        this.helpStates.put(player, new PortalHelperState(gun, end, helper, HELPER_DELAY));
    }

    private static class PortalHelperState {
        private final UUID lastHelpedGun;
        private final PortalEnd lastHelpedEnd;
        private final AbstractPortalHelper lastHelpedHelper;
        private long nextHelp;

        private PortalHelperState(UUID gun, PortalEnd end, AbstractPortalHelper helper, long nextHelp) {
            this.lastHelpedGun = gun;
            this.lastHelpedEnd = end;
            this.lastHelpedHelper = helper;
            this.nextHelp = nextHelp;
        }

        private void tick() {
            if(this.nextHelp > 0)
                this.nextHelp--;
        }

        private boolean isBasicallySame(UUID gun, PortalEnd end, World level, BlockPos pos, Direction face) {
            if(!(this.lastHelpedHelper instanceof PortalHelperRepresentation))
                return false;

            boolean sameGun = gun.equals(this.lastHelpedGun);
            boolean sameEnd = end.equals(this.lastHelpedEnd);
            boolean sameHelper = ((PortalHelperRepresentation) this.lastHelpedHelper).isInside(level, pos, face);
            return sameGun && sameEnd && sameHelper;
        }

        private boolean isBasicallySame(UUID gun, PortalEnd end, VolatilePortalHelper helper) {
            if(!(this.lastHelpedHelper instanceof VolatilePortalHelper))
                return false;

            boolean sameGun = gun.equals(this.lastHelpedGun);
            boolean sameEnd = end.equals(this.lastHelpedEnd);
            boolean sameHelper = this.lastHelpedHelper.equals(helper);
            return sameGun && sameEnd && sameHelper;
        }
    }
}