package net.portalmod.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlockStructureHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.blocks.CustomPushBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Pretty much all copied over from the Carpet mod
@Mixin(PistonBlockStructureHelper.class)
public class PistonBlockStructureHelperMixin {

    @Shadow @Final private World level;
    @Shadow @Final private Direction pushDirection;

    // fields that are needed because @Redirects cannot capture locals
    @Unique
    private BlockPos pos_addBlockLine;
    @Unique
    private BlockPos behindPos_addBlockLinea;

    @Inject(
            method = "addBlockLine",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(
                    value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
            )
    )
    private void captureBlockLinePositions(BlockPos p_177251_1_, Direction p_177251_2_, CallbackInfoReturnable<Boolean> cir, BlockState blockstate, int i, BlockState oldState, BlockPos blockpos) {
        pos_addBlockLine = blockpos.relative(pushDirection);
        behindPos_addBlockLinea = blockpos;
    }

    @Redirect(
            method = "addBlockLine",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;canStickTo(Lnet/minecraft/block/BlockState;)Z"
            )
    )
    private boolean onAddBlockLineCanStickToEachOther(BlockState state, BlockState behindState) {
        Block block = state.getBlock();
        if (block instanceof CustomPushBehavior) {
            return ((CustomPushBehavior) block).isStickyToNeighbor(level, pos_addBlockLine, state, behindPos_addBlockLinea, behindState, pushDirection.getOpposite(), pushDirection);
        }

        return state.canStickTo(behindState);
    }

    // fields that are needed because @Redirects cannot capture locals
    @Unique private Direction dir_addBranchingBlocks;
    @Unique private BlockPos neighborPos_addBranchingBlocks;

    @Inject(
            method = "addBranchingBlocks",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(
                    value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
            )
    )
    private void captureNeighborPositions(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState state, Direction[] dirs, int i, int j, Direction dir, BlockPos neighborPos) {
        dir_addBranchingBlocks = dir;
        neighborPos_addBranchingBlocks = neighborPos;
    }

    @Redirect(
            method = "addBranchingBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;canStickTo(Lnet/minecraft/block/BlockState;)Z"
            )
    )
    private boolean onAddBranchingBlocksCanStickToEachOther(BlockState neighborState, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof CustomPushBehavior) {
            return ((CustomPushBehavior) block).isStickyToNeighbor(level, pos, state, neighborPos_addBranchingBlocks, neighborState, dir_addBranchingBlocks, pushDirection);
        }

        return neighborState.canStickTo(state);
    }
}
