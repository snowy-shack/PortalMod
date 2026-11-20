package net.portalmod.common.sorted.portal;

import net.portalmod.common.sorted.panel.PortalHelper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PortalHelperRepresentation implements AbstractPortalHelper {
    public final BlockPos lastHelpedPos;
    public final Direction lastHelpedFace;

    public PortalHelperRepresentation(BlockPos pos, Direction face) {
        this.lastHelpedPos = pos;
        this.lastHelpedFace = face;
    }

    public boolean isInside(Level level, BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        BlockState panelState = level.getBlockState(this.lastHelpedPos);
        Block panelBlock = panelState.getBlock();

        if(block instanceof PortalHelper && panelBlock instanceof PortalHelper) {
            PortalHelper oldHelper = (PortalHelper) panelBlock;
            boolean samePanel = oldHelper.containsBlock(panelState, this.lastHelpedPos, pos, level);
            boolean sameFace = face.equals(this.lastHelpedFace);
            return samePanel && sameFace;
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PortalHelperRepresentation))
            return false;

        PortalHelperRepresentation other = (PortalHelperRepresentation)obj;
        return this.lastHelpedPos.equals(other.lastHelpedPos) && this.lastHelpedFace.equals(other.lastHelpedFace);
    }
}