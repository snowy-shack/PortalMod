package net.portalmod.common.blocks;

import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.PortalGunInteraction;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class PushDoorBlock extends DoorBlock implements InteractKeyInteractable {
    private void playCloseSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundInit.PUSH_DOOR_CLOSE.get(), SoundSource.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    private void playOpenSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundInit.PUSH_DOOR_OPEN.get(), SoundSource.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player playerEntity, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        return interact(blockState, world, blockPos, blockRayTraceResult) ? InteractionResult.sidedSuccess(world.isClientSide) : InteractionResult.PASS;
    }

    public boolean interact(BlockState blockState, Level world, BlockPos blockPos, BlockHitResult blockRayTraceResult) {
        Direction clickedFace = blockRayTraceResult.getDirection();
        Direction facing = blockState.getValue(FACING);
        Direction doorFront = !blockState.getValue(OPEN)
                ? facing.getOpposite()
                : blockState.getValue(HINGE) == DoorHingeSide.LEFT
                        ? facing.getClockWise()
                        : facing.getCounterClockWise();

        if (clickedFace == doorFront) {
            if (!blockState.getValue(OPEN)) open(blockState, world, blockPos);
            if (blockState.getValue(OPEN)) close(blockState, world, blockPos);

            world.getBlockTicks().scheduleTick(blockPos, this, 20);

            return true;
        }

        return false;
    }

    @Override
    public boolean interactKeyInteract(Player player, BlockHitResult rayHit) {
        if (withinInteractRange(player, rayHit)) {
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.OPEN_DOOR).blockHit(rayHit).build());
            return true;
        }
        return false;
    }

    public PushDoorBlock(Properties p_i48413_1_) {
        super(p_i48413_1_);
    }

    @Override
    public void fallOn(Level p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
        super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel world, BlockPos blockPos, Random random) {
        // Get whether there is a player within 1 block
        boolean playerNearby = !world.getEntitiesOfClass(
                Player.class,
                new AABB(blockPos).inflate(1),
                player -> !player.isSpectator()
        ).isEmpty();

        if (playerNearby) {
            world.getBlockTicks().scheduleTick(blockPos, this, 5);
            return;
        }
        // Close
        if (blockState.getValue(OPEN)) close(blockState, world, blockPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = world.hasNeighborSignal(pos) ||
                world.hasNeighborSignal(pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));

        if (powered != state.getValue(POWERED)) {

            if (powered != state.getValue(OPEN)) {
                if (powered) {
                    playOpenSound(world, pos);
                } else {
                    playCloseSound(world, pos);
                }
            }

            world.setBlock(pos, state
                    .setValue(POWERED, powered)
                    .setValue(OPEN, powered), 2);
        }
    }

    private void open(BlockState blockState, Level world, BlockPos blockPos) {
        blockState = blockState.cycle(OPEN);
        world.setBlock(blockPos, blockState, 10);
        this.playOpenSound(world, blockPos);
    }

    private void close(BlockState blockState, Level world, BlockPos blockPos) {
        if (blockState.getValue(POWERED))
            return;

        blockState = blockState.cycle(OPEN); // Close the door
        world.setBlock(blockPos, blockState, 10);
        this.playCloseSound(world, blockPos);
    }


    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("push_door", list);
    }
}
