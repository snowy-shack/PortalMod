package net.portalmod.common.sorted.cubedropper;

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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

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
    public VoxelShape getShape(BlockState blockState, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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

    public boolean isCornerPlaceable(BlockPlaceContext context, QuadBlockCorner corner) {
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
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
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
            BlockEntity blockEntity = world.getBlockEntity(getMainPosition(state, pos));
            if (blockEntity instanceof CubeDropperTileEntity) {
                ((CubeDropperTileEntity) blockEntity).resetDropper();
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        BlockEntity tileEntity = world.getBlockEntity(this.getMainPosition(blockState, pos));
        if (!(tileEntity instanceof CubeDropperTileEntity)) return InteractionResult.FAIL;
        CubeDropperTileEntity dropperEntity = (CubeDropperTileEntity) tileEntity;

        if (item instanceof SpawnEggItem) {
            dropperEntity.onEggClick(itemStack, player);
            player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1, 1);
            return InteractionResult.SUCCESS;
        }

        if (item instanceof WrenchItem) {
            dropperEntity.onWrenchClick(player);
            WrenchItem.playUseSound(world, result.getLocation());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public void setOpen(boolean open, BlockState blockState, Level world, BlockPos pos) {
        this.setBlockStateValue(OPEN, open, blockState, world, pos);
        world.playSound(null, pos, open ? SoundInit.CUBE_DROPPER_OPEN.get() : SoundInit.CUBE_DROPPER_CLOSE.get(), SoundSource.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
    }

    @Override
    public void onRemove(BlockState blockState, Level world, BlockPos pos, BlockState newState, boolean b) {
        if (isMainBlock(blockState) && !blockState.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CubeDropperTileEntity) {
                ((CubeDropperTileEntity) blockEntity).onRemove();
            }
        }
        super.onRemove(blockState, world, pos, newState, b);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CORNER, HALF, OPEN);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return this.isMainBlock(state);
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
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
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("cube_dropper", list);
    }
}
