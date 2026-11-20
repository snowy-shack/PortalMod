package net.portalmod.mixins;

import net.portalmod.core.util.StickyBlocks;

import net.portalmod.core.interfaces.StickyBlock;

import net.portalmod.common.blocks.CustomPushBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

// Pretty much all copied over from the Carpet mod
@Mixin(PistonStructureResolver.class)
public class PistonBlockStructureHelperMixin {

    @Shadow @Final private Level level;
    @Shadow @Final private Direction pushDirection;

    // Redirects cannot capture locals, so remember the positions as vanilla looks them up.
    @Unique
    private BlockPos pos_addBlockLine;
    @Unique
    private BlockPos behindPos_addBlockLinea;

    @Redirect(
            method = "addBlockLine",
            at = @At(
                    value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState captureBlockLinePositions(Level level, BlockPos behindPos) {
        pos_addBlockLine = behindPos.relative(pushDirection);
        behindPos_addBlockLinea = behindPos;
        return level.getBlockState(behindPos);
    }

    @Redirect(
            method = "addBlockLine",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;canStickToEachOther(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z"
            )
    )
    private boolean onAddBlockLineCanStickToEachOther(BlockState state, BlockState behindState) {
        Block block = state.getBlock();
        if (block instanceof CustomPushBehavior) {
            return ((CustomPushBehavior) block).isStickyToNeighbor(level, pos_addBlockLine, state, behindPos_addBlockLinea, behindState, pushDirection.getOpposite(), pushDirection);
        }

        return StickyBlocks.canStickTo(state, behindState);
    }

    @Unique private Direction dir_addBranchingBlocks;
    @Unique private BlockPos neighborPos_addBranchingBlocks;

    @Redirect(
            method = "addBranchingBlocks",
            at = @At(
                    value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState captureNeighborPositions(Level level, BlockPos neighborPos, BlockPos pos) {
        neighborPos_addBranchingBlocks = neighborPos;
        dir_addBranchingBlocks = Direction.fromDelta(neighborPos.getX() - pos.getX(), neighborPos.getY() - pos.getY(), neighborPos.getZ() - pos.getZ());
        return level.getBlockState(neighborPos);
    }

    @Redirect(
            method = "addBranchingBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;canStickToEachOther(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z"
            )
    )
    private boolean onAddBranchingBlocksCanStickToEachOther(BlockState neighborState, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof CustomPushBehavior) {
            return ((CustomPushBehavior) block).isStickyToNeighbor(level, pos, state, neighborPos_addBranchingBlocks, neighborState, dir_addBranchingBlocks, pushDirection);
        }

        return StickyBlocks.canStickTo(neighborState, state);
    }

    /** Lets mod blocks be sticky like slime (Forge: IForgeBlock#isStickyBlock). */
    @Inject(method = "isSticky", at = @At("HEAD"), cancellable = true)
    private static void pmIsSticky(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof StickyBlock sticky)
            cir.setReturnValue(sticky.isStickyBlock(state));
    }
}
