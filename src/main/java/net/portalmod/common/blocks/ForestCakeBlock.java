package net.portalmod.common.blocks;

import net.minecraft.util.RandomSource;
import net.portalmod.core.init.ParticleInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

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
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        InteractionResult used = super.use(blockState, world, pos, player, hand, result);
        if (used == InteractionResult.SUCCESS) {
            boolean ateCandle = blockState.getValue(BITES) == 0;
            world.playSound(player, pos, (ateCandle ? SoundInit.CAKE_EAT_CANDLE.get() : SoundEvents.GENERIC_EAT), SoundSource.PLAYERS, 1, ModUtil.randomSoundPitch());
        }
        return used;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        switch (blockState.getValue(BITES)) {
            case 0: return Shapes.or(BASE, CANDLE);
            case 1: return BASE;
            case 2: return Shapes.or(HALF, PART_1);
            case 3: return HALF;
            case 4: return Shapes.or(QUARTER, PART_2);
            case 5: return QUARTER;
            case 6: return PART_3;
        }
        return super.getShape(blockState, p_220053_2_, p_220053_3_, p_220053_4_);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
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
