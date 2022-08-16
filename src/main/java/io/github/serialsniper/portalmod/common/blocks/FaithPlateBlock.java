package io.github.serialsniper.portalmod.common.blocks;

import io.github.serialsniper.portalmod.client.screens.FaithPlateConfigScreen;
import io.github.serialsniper.portalmod.common.blockentities.FaithPlateTileEntity;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import io.github.serialsniper.portalmod.core.util.FaithPlateParabola;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FaithPlateBlock extends Block {
    public FaithPlateBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypeInit.FAITHPLATE.get().create();
    }

    @Override
    public void fallOn(World level, BlockPos pos, Entity entity, float fallDistance) {}

    @Override
    public void updateEntityAfterFallOn(IBlockReader level, Entity entity) {
//        double angle = 0.541052068118;
//        double velocity = 2.3;

        int i = MathHelper.floor(entity.position().x);
        int j = MathHelper.floor(entity.position().y - (double)0.2F);
        int k = MathHelper.floor(entity.position().z);
        BlockPos blockpos = new BlockPos(i, j, k);
        if(entity.level.isEmptyBlock(blockpos)) {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = entity.level.getBlockState(blockpos1);
            if(blockstate.collisionExtendsVertically(entity.level, blockpos1, entity))
                blockpos = blockpos1;
        }

        // todo use nbt to launch

        FaithPlateTileEntity be = ((FaithPlateTileEntity)level.getBlockEntity(blockpos));
        FaithPlateParabola parabola = new FaithPlateParabola(be.getTargetPos(), be.getTargetSide(), Double.NEGATIVE_INFINITY);
        double angle = parabola.getAngle();
        double velocity = parabola.getVelocity();

        entity.setPos(Math.floor(entity.getX()) + 0.5f, Math.floor(entity.getY()), Math.floor(entity.getZ()) + 0.5f);
        entity.setDeltaMovement(new Vector3d(
                Math.cos(angle) * velocity,
                Math.sin(angle) * velocity,
                0
        ));
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(level.isClientSide && player.getItemInHand(hand).getItem() == ItemInit.WRENCH.get() && level.getBlockEntity(pos) instanceof FaithPlateTileEntity) {
            Minecraft.getInstance().setScreen(new FaithPlateConfigScreen(pos));
            return ActionResultType.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, rayTraceResult);
    }
}