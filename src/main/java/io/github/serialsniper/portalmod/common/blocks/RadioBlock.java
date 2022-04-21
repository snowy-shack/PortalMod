package io.github.serialsniper.portalmod.common.blocks;

import io.github.serialsniper.portalmod.common.blockentities.RadioBlockTileEntity;
import io.github.serialsniper.portalmod.core.enums.RadioState;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.StateContainer.*;
import net.minecraft.state.properties.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;

public class RadioBlock extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<RadioState> STATE = EnumProperty.create("state", RadioState.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	VoxelShape AABB = Block.box(3.5, 0, 6, 12.5, 6, 10);
	VoxelShape AABB_SIDE = Block.box(6, 0, 3.5, 10, 6, 12.5);
	
	public RadioBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(STATE, RadioState.OFF)
				.setValue(POWERED, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return TileEntityTypeInit.RADIO.get().create();
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, STATE, POWERED);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
		switch(state.getValue(FACING)) {
			case WEST:
			case EAST:
				return AABB_SIDE;
				
			default:
				return AABB;
		}
	}
	
	public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
		return canSupportCenter(world, pos.below(), Direction.UP);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult rayTraceResult) {
		if(world.isClientSide())
			return ActionResultType.SUCCESS;

		((RadioBlockTileEntity)world.getBlockEntity(pos)).switchManual();
		return ActionResultType.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos targetPos, boolean b) {
		if(world.isClientSide()) return;

		if(!state.canSurvive(world, pos)) {
			dropResources(state, world, pos);
			world.removeBlock(pos, false);
			return;
		}

		RadioBlockTileEntity tile = (RadioBlockTileEntity) world.getBlockEntity(pos);
		if(world.hasNeighborSignal(pos)) {
			world.setBlock(pos, state.setValue(POWERED, true), 2);
			tile.play();
		} else {
			if(state.getValue(POWERED)) {
				world.setBlock(pos, state.setValue(POWERED, false), 2);
				if(world.dimension() != World.END)
					tile.stop();
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		boolean isPowered = context.getLevel().hasNeighborSignal(context.getClickedPos());
		RegistryKey<World> dimension = context.getLevel().dimension();

		BlockState state = this.stateDefinition.any()
				.setValue(FACING, context.getHorizontalDirection())
				.setValue(POWERED, isPowered);

		if(dimension == World.OVERWORLD)
			state = state.setValue(STATE, isPowered ? RadioState.ON : RadioState.OFF);
		else if(dimension == World.END)
			state = state.setValue(STATE, isPowered ? RadioState.ACTIVE : RadioState.INACTIVE);
		return state;
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}