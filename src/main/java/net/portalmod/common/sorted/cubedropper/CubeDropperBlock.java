package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeSpawnEggItem;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            pos = pos.relative(Direction.WEST);
        }
        if (!blockState.getValue(CORNER).isUp()) {
            pos = pos.relative(Direction.NORTH);
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            pos = pos.relative(Direction.UP);
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
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
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
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

        world.setBlockAndUpdate(pos.relative(leftRight), blockState.setValue(CORNER, oppositeLeftRight));

        world.setBlockAndUpdate(pos.relative(upDown), blockState.setValue(CORNER, oppositeUpDown));

        world.setBlockAndUpdate(pos.relative(upDown).relative(leftRight), blockState.setValue(CORNER, diagonal));

        world.setBlockAndUpdate(pos.relative(vertical), blockState.setValue(HALF, oppositeHalf));

        world.setBlockAndUpdate(pos.relative(vertical).relative(leftRight), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, oppositeLeftRight));

        world.setBlockAndUpdate(pos.relative(vertical).relative(upDown), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, oppositeUpDown));

        world.setBlockAndUpdate(pos.relative(vertical).relative(upDown).relative(leftRight), blockState.setValue(HALF, oppositeHalf).setValue(CORNER, diagonal));
    }

    @Override
    public boolean isMainBlock(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER && state.getValue(CORNER) == QuadBlockCorner.UP_LEFT;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();

        if (context.getClickedFace() != Direction.DOWN) {
            return null;
        }

        List<QuadBlockCorner> possibleCorners = new ArrayList<>();

        if (canPlace(new BlockPos[]{
                pos.relative(Direction.EAST),
                pos.relative(Direction.SOUTH),
                pos.relative(Direction.SOUTH).relative(Direction.EAST),
                pos.below(),
                pos.below().relative(Direction.EAST),
                pos.below().relative(Direction.SOUTH),
                pos.below().relative(Direction.SOUTH).relative(Direction.EAST)
        }, context, world)) {
            possibleCorners.add(QuadBlockCorner.UP_LEFT);
        }

        if (canPlace(new BlockPos[]{
                pos.relative(Direction.WEST),
                pos.relative(Direction.SOUTH),
                pos.relative(Direction.SOUTH).relative(Direction.WEST),
                pos.below(),
                pos.below().relative(Direction.WEST),
                pos.below().relative(Direction.SOUTH),
                pos.below().relative(Direction.SOUTH).relative(Direction.WEST)
        }, context, world)) {
            possibleCorners.add(QuadBlockCorner.UP_RIGHT);
        }

        if (canPlace(new BlockPos[]{
                pos.relative(Direction.WEST),
                pos.relative(Direction.NORTH),
                pos.relative(Direction.NORTH).relative(Direction.WEST),
                pos.below(),
                pos.below().relative(Direction.WEST),
                pos.below().relative(Direction.NORTH),
                pos.below().relative(Direction.NORTH).relative(Direction.WEST)
        }, context, world)) {
            possibleCorners.add(QuadBlockCorner.DOWN_RIGHT);
        }

        if (canPlace(new BlockPos[]{
                pos.relative(Direction.EAST),
                pos.relative(Direction.NORTH),
                pos.relative(Direction.NORTH).relative(Direction.EAST),
                pos.below(),
                pos.below().relative(Direction.EAST),
                pos.below().relative(Direction.NORTH),
                pos.below().relative(Direction.NORTH).relative(Direction.EAST)
        }, context, world)) {
            possibleCorners.add(QuadBlockCorner.DOWN_LEFT);
        }

        if (possibleCorners.isEmpty()) {
            return null;
        }

        QuadBlockCorner preferredCorner = QuadBlockCorner.values()[MathHelper.positiveModulo(MathHelper.floor(player.yRot / 90 + 1), 4)];
        if (possibleCorners.contains(preferredCorner)) {
            return defaultBlockState().setValue(CORNER, preferredCorner);
        }
        return defaultBlockState().setValue(CORNER, possibleCorners.get(0));
    }

    public static boolean canPlace(BlockPos[] posArray, BlockItemUseContext context, World world) {
        for (BlockPos pos : posArray) {
            if (!world.getBlockState(pos).canBeReplaced(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (world.isClientSide) {
            return ActionResultType.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();
        TileEntity tileEntity = world.getBlockEntity(this.getMainPosition(blockState, pos));
        if (!(tileEntity instanceof CubeDropperTileEntity)) {
            return ActionResultType.FAIL;
        }

        CubeDropperTileEntity dropperEntity = (CubeDropperTileEntity) tileEntity;
        Optional<EntityType<?>> entityType = dropperEntity.getEntityType();

        if (item instanceof SpawnEggItem) {
            EntityType<?> spawnEggType = ((SpawnEggItem) item).getType(itemStack.getTag());

            // Remove new egg and give previous egg
            if (!player.isCreative()) {
                itemStack.shrink(1);
                entityType.ifPresent(type -> player.addItem(new ItemStack(ForgeSpawnEggItem.fromEntityType(type))));
            }

            dropperEntity.removeAllEntities();
            dropperEntity.setEntityNBT(spawnEggType);
            player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1, 1);
            return ActionResultType.SUCCESS;
        }

        if (item instanceof WrenchItem && dropperEntity.hasEntityNBT()) {

            // Remove current spawned cube
            if (dropperEntity.entityUUIDs.size() > 1) {
                dropperEntity.fizzleCube(dropperEntity.entityUUIDs.get(0));

            // Clear entity type of dropper
            } else {
                dropperEntity.removeAllEntities();
                if (!player.isCreative()) {
                    entityType.ifPresent(type -> player.addItem(new ItemStack(ForgeSpawnEggItem.fromEntityType(type))));
                }

                dropperEntity.removeEntityNBT();
            }

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
                CubeDropperTileEntity dropperEntity = (CubeDropperTileEntity) blockEntity;
                dropperEntity.removeAllEntities();

                // Drop spawn egg if present
                BlockPos mainPosition = this.getMainPosition(blockState, pos);
                Optional<EntityType<?>> entityType = dropperEntity.getEntityType();
                entityType.ifPresent(type ->
                        InventoryHelper.dropItemStack(world, mainPosition.getX(), mainPosition.getY(), mainPosition.getZ(), new ItemStack(ForgeSpawnEggItem.fromEntityType(type)))
                );
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
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("cube_dropper", list);
    }
}
