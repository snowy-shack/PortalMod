package net.portalmod.common.sorted.portal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.panel.PortalHelper;

public class PortalHelperRepresentation implements AbstractPortalHelper {
    public final BlockPos lastHelpedPos;
    public final Direction lastHelpedFace;

    public PortalHelperRepresentation(BlockPos pos, Direction face) {
        this.lastHelpedPos = pos;
        this.lastHelpedFace = face;
    }

    public boolean isInside(World level, BlockPos pos, Direction face) {
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