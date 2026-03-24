package net.portalmod.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.PortalGunInteraction;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class PushDoorBlock extends DoorBlock implements EKeyInteractable {
    private void playCloseSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundInit.PUSH_DOOR_CLOSE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    private void playOpenSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundInit.PUSH_DOOR_OPEN.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        return interact(blockState, world, blockPos, blockRayTraceResult) ? ActionResultType.sidedSuccess(world.isClientSide) : ActionResultType.PASS;
    }

    public boolean interact(BlockState blockState, World world, BlockPos blockPos, BlockRayTraceResult blockRayTraceResult) {
        Direction clickedFace = blockRayTraceResult.getDirection();
        Direction doorFront = blockState.getValue(OPEN)
                ? blockState.getValue(FACING).getOpposite().getClockWise()
                : blockState.getValue(FACING).getOpposite();

        if (clickedFace == doorFront) {
            if (!blockState.getValue(OPEN)) open(blockState, world, blockPos);
            if (blockState.getValue(OPEN)) close(blockState, world, blockPos);

            world.getBlockTicks().scheduleTick(blockPos, this, 20);

            return true;
        }

        return false;
    }

    @Override
    public boolean eKeyInteract(PlayerEntity player, BlockRayTraceResult rayHit) {
        PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.OPEN_DOOR).blockHit(rayHit).build());
        return true;
    }

    public PushDoorBlock(Properties p_i48413_1_) {
        super(p_i48413_1_);
    }

    @Override
    public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
        super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
    }

    @Override
    public void tick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        // Get whether there is a player within 1 block
        boolean playerNearby = !world.getEntitiesOfClass(
                PlayerEntity.class,
                new AxisAlignedBB(blockPos).inflate(1),
                player -> !player.isSpectator()
        ).isEmpty();

        if (playerNearby) {
            world.getBlockTicks().scheduleTick(blockPos, this, 5);
            return;
        }
        // Close
        if (blockState.getValue(OPEN)) close(blockState, world, blockPos);
    }

    private void open(BlockState blockState, World world, BlockPos blockPos) {
        blockState = blockState.cycle(OPEN);
        world.setBlock(blockPos, blockState, 10);
        this.playOpenSound(world, blockPos);
    }

    private void close(BlockState blockState, World world, BlockPos blockPos) {
        blockState = blockState.cycle(OPEN); // Close the door
        world.setBlock(blockPos, blockState, 10);
        this.playCloseSound(world, blockPos);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("push_door", list);
    }
}
