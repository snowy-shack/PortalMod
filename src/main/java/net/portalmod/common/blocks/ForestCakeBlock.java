package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.core.init.ParticleInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.Vec3;

import java.util.Random;

public class ForestCakeBlock extends CakeBlock {

    public static final VoxelShape CANDLE = Block.box(7, 8, 7, 9, 14, 9);
    public static final VoxelShape BASE = Block.box(2, 0, 2, 14, 8, 14);
    public static final VoxelShape HALF = Block.box(2, 0, 2, 10, 8, 14);
    public static final VoxelShape QUARTER = Block.box(2, 0, 2, 10, 8, 8);
    public static final VoxelShape PART_1 = Block.box(10, 0, 8, 14, 8, 14);
    public static final VoxelShape PART_2 = Block.box(2, 0, 8, 6, 8, 14);
    public static final VoxelShape PART_3 = Block.box(6, 0, 2, 10, 8, 8);

    public ForestCakeBlock(Properties p_i48434_1_) {
        super(p_i48434_1_);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        ActionResultType used = super.use(blockState, world, pos, player, hand, result);
        if (used.consumesAction()) {
            boolean ateCandle = blockState.getValue(BITES) == 0;
            world.playSound(player, pos, (ateCandle ? SoundInit.CAKE_EAT_CANDLE : SoundInit.CAKE_EAT).get(), SoundCategory.PLAYERS, 1, 1);
        }
        return used;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        switch (blockState.getValue(BITES)) {
            case 0: return VoxelShapes.or(BASE, CANDLE);
            case 1: return BASE;
            case 2: return VoxelShapes.or(HALF, PART_1);
            case 3: return HALF;
            case 4: return VoxelShapes.or(QUARTER, PART_2);
            case 5: return QUARTER;
            case 6: return PART_3;
        }
        return super.getShape(blockState, p_220053_2_, p_220053_3_, p_220053_4_);
    }

    @Override
    public void animateTick(BlockState blockState, World level, BlockPos pos, Random randomSource) {
        if (blockState.getValue(BITES) > 0) {
            return;
        }

        Vec3 vec3 = new Vec3(0.5, 1.0, 0.5).add(pos);
        float f = randomSource.nextFloat();
        if (f < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
        }
        level.addParticle(ParticleInit.SMALL_FLAME.get(), vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
    }
}
