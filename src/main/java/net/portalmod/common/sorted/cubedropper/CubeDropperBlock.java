package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.button.QuadBlockCorner;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.*;

public class CubeDropperBlock extends MultiBlock {

    public static final EnumProperty<QuadBlockCorner> CORNER = EnumProperty.create("corner", QuadBlockCorner.class);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final BiHashMap<DoubleBlockHalf, QuadBlockCorner, VoxelShapeGroup> SHAPE = new BiHashMap<>();

    public CubeDropperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CORNER, QuadBlockCorner.UP_LEFT)
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(OPEN, false));
        this.initAABBs();
    }

    private void initAABBs() {
        VoxelShapeGroup upper = new VoxelShapeGroup.Builder()
                .add(0, 6, 0, 16, 16, 8)
                .add(0, 6, 0, 8, 16, 16)
                .add(2, 0, 2, 16, 6, 8)
                .add(2, 0, 2, 8, 6, 16)
                .add(0, 15, 0, 16, 16, 16)
                .build();
        VoxelShapeGroup lower = new VoxelShapeGroup.Builder()
                .add(2, 0, 2, 16, 16, 8)
                .add(2, 0, 2, 8, 16, 16)
                .addPart("closed", 8, 2, 8, 16, 3, 16)
                .build();

        for(QuadBlockCorner corner : QuadBlockCorner.values()) {
            for(DoubleBlockHalf half : DoubleBlockHalf.values()) {
                Mat4 matrix = Mat4.identity();
                matrix.translate(new Vec3(.5));
                matrix.rotateDeg(new Vector3f(0, 1, 0), corner.getRot() - 90);
                matrix.translate(new Vec3(-.5));
                VoxelShapeGroup group = half == DoubleBlockHalf.LOWER ? lower : upper;

                SHAPE.put(half, corner, group.clone().transform(matrix));
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return SHAPE.get(blockState.getValue(HALF), blockState.getValue(CORNER)).getVariant(blockState.getValue(OPEN) ? "" : "closed");
    }

    /*
               North

     West    main  UR    East
              DL   DR

               South
     */

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        if (!blockState.getValue(CORNER).isLeft()) {
            pos = pos.west();
        }
        if (!blockState.getValue(CORNER).isUp()) {
            pos = pos.north();
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            pos = pos.above();
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState mainState, BlockPos mainPos) {
        return new ArrayList<>(Arrays.asList(
                mainPos.relative(Direction.EAST),
                mainPos.relative(Direction.SOUTH),
                mainPos.relative(Direction.SOUTH).relative(Direction.EAST),
                mainPos.below(),
                mainPos.below().relative(Direction.EAST),
                mainPos.below().relative(Direction.SOUTH),
                mainPos.below().relative(Direction.SOUTH).relative(Direction.EAST)
        ));
    }

    @Override
    public Map<BlockPos, BlockState> getOtherParts(BlockState blockState, BlockPos pos) {
        QuadBlockCorner corner = blockState.getValue(CORNER);
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = corner.isLeft();
        boolean isUp = corner.isUp();

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction leftRight = isLeft ? Direction.EAST : Direction.WEST;
        Direction upDown = isUp ? Direction.SOUTH : Direction.NORTH;

        DoubleBlockHalf oppositeHalf = isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        QuadBlockCorner oppositeLeftRight = corner.mirrorLeftRight();
        QuadBlockCorner oppositeUpDown = corner.mirrorUpDown();
        QuadBlockCorner diagonal = corner.mirrorUpDown().mirrorLeftRight();

        HashMap<BlockPos, BlockState> map = new HashMap<>();

        map.put(pos.relative(leftRight), blockState.setValue(CORNER, oppositeLeftRight));

        map.put(pos.relative(upDown), blockState.setValue(CORNER, oppositeUpDown));

        map.put(pos.relative(upDown).relative(leftRight), blockState.setValue(CORNER, diagonal));

        map.put(pos.relative(vertical), blockState.setValue(HALF, oppositeHalf));

        map.put(pos.relative(vertical).relative(leftRight), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, oppositeLeftRight));

        map.put(pos.relative(vertical).relative(upDown), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, oppositeUpDown));

        map.put(pos.relative(vertical).relative(upDown).relative(leftRight), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, diagonal));

        return map;
    }

    @Override
    public boolean isSamePart(BlockState one, BlockState two) {
        return one.getValue(HALF) == two.getValue(HALF)
                && one.getValue(CORNER) == two.getValue(CORNER);
    }

    @Override
    public void addMainBlockProperties(Map<Property<?>, Comparable<?>> map) {
        map.put(HALF, DoubleBlockHalf.UPPER);
        map.put(CORNER, QuadBlockCorner.UP_LEFT);
    }

    @Override
    public boolean lookDirectionInfluencesLocation() {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getClickedFace() != Direction.DOWN) return null;

        boolean prefersLeft = clickedOnPositiveHalf(context, Direction.Axis.X);
        boolean prefersUp = clickedOnPositiveHalf(context, Direction.Axis.Z);

        boolean[] flipUp   = {false, false, true, true};
        boolean[] flipLeft = {false, true, false, true};

        for (int i = 0; i < 4; i++) {
            QuadBlockCorner corner = QuadBlockCorner.getCorner(prefersUp ^ flipUp[i], prefersLeft ^ flipLeft[i]);

            if (this.isCornerPlaceable(context, corner)) {
                return this.defaultBlockState().setValue(CORNER, corner);
            }
        }

        return null;
    }

    public boolean isCornerPlaceable(BlockItemUseContext context, QuadBlockCorner corner) {
        BlockPos topLeftPos = context.getClickedPos();

        if (!corner.isLeft()) topLeftPos = topLeftPos.west();
        if (!corner.isUp()) topLeftPos = topLeftPos.north();

        BlockPos[] topLeftOffsets = {
                topLeftPos,
                topLeftPos.east(),
                topLeftPos.south(),
                topLeftPos.south().east(),
                topLeftPos.below(),
                topLeftPos.below().east(),
                topLeftPos.below().south(),
                topLeftPos.below().south().east(),
        };

        return Arrays.stream(topLeftOffsets).allMatch(pos -> ModUtil.canPlaceAt(context, pos));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        super.neighborChanged(state, world, pos, block, neighborPos, b);
        if (world.isClientSide) return;

        // Reset when powered from "below"
        boolean isPowered = getAllPositions(state, pos).stream()
                .filter(checkingPos -> {
                    BlockState blockState = world.getBlockState(checkingPos);
                    return blockState.getBlock() instanceof CubeDropperBlock && blockState.getValue(HALF) == DoubleBlockHalf.UPPER;
                })
                .anyMatch(checkingPos -> world.hasSignal(checkingPos.above(), Direction.DOWN));

        if (isPowered) {
            TileEntity blockEntity = world.getBlockEntity(getMainPosition(state, pos));
            if (blockEntity instanceof CubeDropperTileEntity) {
                ((CubeDropperTileEntity) blockEntity).resetDropper();
            }
        }
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (world.isClientSide) {
            return ActionResultType.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        TileEntity tileEntity = world.getBlockEntity(this.getMainPosition(blockState, pos));
        if (!(tileEntity instanceof CubeDropperTileEntity)) return ActionResultType.FAIL;
        CubeDropperTileEntity dropperEntity = (CubeDropperTileEntity) tileEntity;

        if (item instanceof SpawnEggItem) {
            dropperEntity.onEggClick(itemStack, player);
            player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1, 1);
            return ActionResultType.SUCCESS;
        }

        if (item instanceof WrenchItem) {
            dropperEntity.onWrenchClick(player);
            WrenchItem.playUseSound(world, result.getLocation());
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public void setOpen(boolean open, BlockState blockState, World world, BlockPos pos) {
        this.setBlockStateValue(OPEN, open, blockState, world, pos);
        world.playSound(null, pos, open ? SoundInit.CUBE_DROPPER_OPEN.get() : SoundInit.CUBE_DROPPER_CLOSE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
    }

    @Override
    public void onRemove(BlockState blockState, World world, BlockPos pos, BlockState newState, boolean b) {
        if (isMainBlock(blockState) && !blockState.is(newState.getBlock())) {
            TileEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CubeDropperTileEntity) {
                ((CubeDropperTileEntity) blockEntity).onRemove();
            }
        }
        super.onRemove(blockState, world, pos, newState, b);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(CORNER, HALF, OPEN);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return this.isMainBlock(state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return this.isMainBlock(state) ? TileEntityTypeInit.CUBE_DROPPER.get().create() : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        int times = ModUtil.getRotationAmount(rotation);
        return state.setValue(CORNER, state.getValue(CORNER).rotate(times));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        switch (mirror) {
            case FRONT_BACK: return state.setValue(CORNER, state.getValue(CORNER).mirrorLeftRight());
            case LEFT_RIGHT: return state.setValue(CORNER, state.getValue(CORNER).mirrorUpDown());
        }
        return state;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("cube_dropper", list);
    }
}
