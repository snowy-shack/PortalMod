package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.VoxelShapeGroup;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ButtonPedestalBlock extends DoubleBlock {

//    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public boolean stayPressed = false;

    public ButtonPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(ACTIVE, false)
        );
        this.initAABBs();
    }

    private static final Map<DoubleBlockHalf, VoxelShapeGroup> SHAPE = new HashMap<>();

    private static final VoxelShapeGroup LOWER = new VoxelShapeGroup.Builder()
        .add(4, 0, 4, 12, 2, 12)
        .add(5.5, 2.95, 5.5, 10.5, 16, 10.5)
        .add(6, 2, 6, 10, 16, 10)
        .build();

    private static final VoxelShapeGroup UPPER = new VoxelShapeGroup.Builder()
            .add(6, 0, 6, 10, 3, 10)
            .add(5.5, 0, 5.5, 10.5, 3, 10.5)
            .addPart("off", 6, 3, 6, 10, 5, 10)
            .addPart("on", 6, 3, 6, 10, 3.5, 10)
            .build();

    private void initAABBs() {
        for(DoubleBlockHalf half : DoubleBlockHalf.values()) {
            SHAPE.put(half, (half == DoubleBlockHalf.UPPER) ? UPPER.clone() : LOWER.clone());
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(HALF)).getVariant(state.getValue(ACTIVE) ? "on" : "off");
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF, ACTIVE);
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
//        BlockState rotated = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());

        if (world.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState();
        }

        if (world.getBlockState(pos.below()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER);
        }

        return null;
    }
}
