package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.core.init.SoundInit;

import javax.annotation.Nullable;
import java.util.Random;

public class ButtonPedestalBlock extends DoubleBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public boolean stayPressed = false;

    public ButtonPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(ACTIVE, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, ACTIVE);
    }

    public void activate(BlockState blockState, World world, BlockPos pos) {
        if (!blockState.getValue(ACTIVE)) {
            this.setBlockStateValue(ACTIVE, true, blockState, world, pos);
            world.updateNeighborsAt(pos, this);
            world.getBlockTicks().scheduleTick(pos, this, 30);
            this.playSound(world, pos, true);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerWorld world, BlockPos pos, Random random) {
        if (!this.stayPressed && blockState.getValue(ACTIVE)) {
            this.setBlockStateValue(ACTIVE, false, blockState, world, pos);
            world.updateNeighborsAt(pos, this);
            this.playSound(world, pos, false);
        }
    }

    public void playSound(World world, BlockPos pos, boolean activated) {
        world.playSound(null, pos, activated ? SoundInit.SUPER_BUTTON_ACTIVATE.get() : SoundInit.SUPER_BUTTON_DEACTIVATE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        this.activate(blockState, world, pos);
        return blockState.getValue(ACTIVE) ? ActionResultType.FAIL : ActionResultType.sidedSuccess(world.isClientSide);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState rotated = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());

        if (world.getBlockState(pos.above()).canBeReplaced(context)) {
            return rotated;
        }

        if (world.getBlockState(pos.below()).canBeReplaced(context)) {
            return rotated.setValue(HALF, DoubleBlockHalf.UPPER);
        }

        return null;
    }
}
