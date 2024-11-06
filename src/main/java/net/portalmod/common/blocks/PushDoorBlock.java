package net.portalmod.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.core.init.SoundInit;

import java.util.Random;

public class PushDoorBlock extends DoorBlock {
    private void playCloseSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundInit.PUSH_DOOR_CLOSE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    private void playOpenSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundInit.PUSH_DOOR_OPEN.get(), SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (blockRayTraceResult.getDirection() == blockState.getValue(FACING).getOpposite() && !blockState.getValue(OPEN)) {
            blockState = blockState.cycle(OPEN);
            world.setBlock(blockPos, blockState, 10);
            this.playOpenSound(world, blockPos);

            world.getBlockTicks().scheduleTick(blockPos, this, 20);

            return ActionResultType.sidedSuccess(world.isClientSide);
        } else {
            return ActionResultType.PASS;
        }
    }

    public PushDoorBlock(Properties p_i48413_1_) {
        super(p_i48413_1_);
    }

    @Override
    public void tick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        // Get whether there is a player within 3 blocks
        boolean playerNearby = !world.getEntitiesOfClass(PlayerEntity.class,
                new AxisAlignedBB(blockPos).inflate(3),
                player -> !player.isSpectator()).isEmpty();
        if (!playerNearby) {
            if (blockState.getValue(OPEN)) {
                blockState = blockState.cycle(OPEN); // Close the door
                world.setBlock(blockPos, blockState, 10);
                this.playCloseSound(world, blockPos);
            }
        } else world.getBlockTicks().scheduleTick(blockPos, this, 5);
    }
}
