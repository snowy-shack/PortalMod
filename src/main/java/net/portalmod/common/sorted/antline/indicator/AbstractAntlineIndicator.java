package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineActivator;
import net.portalmod.common.sorted.antline.AntlineBlock;
import net.portalmod.common.sorted.antline.AntlineTileEntity;

/**
 * Defines a format for regular indicators and handles the reversed property.
 */
public abstract class AbstractAntlineIndicator extends AntlineDevice implements AntlineActivated, TestElementActivator {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public AbstractAntlineIndicator(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block nBlock, BlockPos nPos, boolean b) {
        super.neighborChanged(state, world, pos, nBlock, nPos, b);

        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        Direction normal = getConnectedDirection(state);
        Direction.Axis axis = normal.getAxis();

        // Check other blocks for power, like buttons
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockState blockState = world.getBlockState(neighborPos);
            Block neighborBlock = blockState.getBlock();

            // Powered by element
            if (neighborBlock instanceof AntlineActivator && ((AntlineActivator) neighborBlock).isActive(blockState)) {
                this.setActive(true, world, pos);
                return;
            }

            // Powered by antline
            if (neighborBlock instanceof AntlineBlock) {
                AntlineTileEntity.Side side = ((AntlineTileEntity) world.getBlockEntity(neighborPos)).getSideMap().get(normal.getOpposite());
                if (side.hasConnection(direction.getOpposite()) && side.isActive()) {
                    this.setActive(true, world, pos);
                    return;
                }
            }
        }

        this.setActive(false, world, pos);
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean b) {
        // First update surrounding antlines, then check for them
        world.updateNeighborsAt(pos, this);
        this.updatePower(state, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVATED);
    }
}
