package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.BlockTagInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static net.portalmod.common.sorted.antline.AntlineTileEntity.Side.Center.FALSE;

public class AntlineBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);  // test

    public AntlineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        TileEntity entity = TileEntityTypeInit.ANTLINE.get().create();
        return entity;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (!canSupportCenter(context.getLevel(),
                context.getClickedPos().relative(context.getClickedFace().getOpposite()),
                context.getClickedFace()))
            return null;
        return super.getStateForPlacement(context);
    }

    public static int modifyConnectedSides(World level, BlockPos pos, Direction dotDirection, AntlineTileEntity tileEntity, boolean add, boolean changePower, Direction powerOrigin) {
        for (Direction adjacentFace : Direction.values()) {
            if (adjacentFace.getAxis() == dotDirection.getAxis()) continue;
            if (adjacentFace.getOpposite() == powerOrigin) continue;

            if (changePower && (!tileEntity.getSideMap().hasSide(dotDirection) || !tileEntity.getSideMap().get(dotDirection).hasConnection(adjacentFace))) continue;

            // For all 4 adjacent faces
            BlockPos neighborPos = pos.relative(adjacentFace);
            BlockState neighborBlockState = level.getBlockState(neighborPos);
            Block neighborBlock = neighborBlockState.getBlock();

            Block cornerNeighborBlock = level.getBlockState(pos.relative(adjacentFace).relative(dotDirection)).getBlock();

            if (neighborBlockState.isRedstoneConductor(level, neighborPos)) { // Solid block; same block
                if (changePower) chainPower(level, pos, adjacentFace, dotDirection.getOpposite(), add);
                else modifyConnection(level, pos, dotDirection, adjacentFace, pos, adjacentFace, dotDirection, add);
                continue;
            }

            if (cornerNeighborBlock instanceof AntlineBlock) { // Non-solid block; corner block
                if (changePower) chainPower(level, pos.relative(adjacentFace).relative(dotDirection), adjacentFace.getOpposite(), dotDirection, add);
                else modifyConnection(level, pos, dotDirection, adjacentFace, pos.relative(adjacentFace).relative(dotDirection), adjacentFace.getOpposite(), dotDirection.getOpposite(), add);
                continue;
            }

            if (neighborBlock instanceof AntlineBlock || neighborBlock.is(BlockTagInit.ANTLINE_CONNECTABLE)) { // Adjacent block
                if (changePower) chainPower(level, pos.relative(adjacentFace), dotDirection, adjacentFace, add);
                else modifyConnection(level, pos, dotDirection, adjacentFace, pos.relative(adjacentFace), dotDirection, adjacentFace.getOpposite(), add);
            }
        }
        return 0;
    }

    private static void modifyConnection(World level, BlockPos posA, Direction sideA, Direction dirA, BlockPos posB, Direction sideB, Direction dirB, boolean connect) {
        AntlineTileEntity tileEntityA = (AntlineTileEntity)level.getBlockEntity(posA);
        AntlineTileEntity.Side dotA = tileEntityA.getSideMap().get(sideA);

        if (level.getBlockState(posB).getBlock() instanceof AntlineBlock) {

            AntlineTileEntity tileEntityB = (AntlineTileEntity)level.getBlockEntity(posB);
            AntlineTileEntity.Side dotB = tileEntityB.getSideMap().get(sideB);

            if ((!connect && dotB.hasConnection(dirB)) || (connect && dotA.isConnectableWith(dirA) && dotB.isConnectableWith(dirB))) {
                if (connect) {
                    dotA.addConnection(dirA);
                    dotB.addConnection(dirB);
                } else {
                    dotA.removeConnection(dirA);
                    dotB.removeConnection(dirB);
                }

                CompoundNBT nbtA = new CompoundNBT();
                nbtA.putByte(sideA.getName(), tileEntityA.getSideMap().get(sideA).getActualValue());
                CompoundNBT nbtB = new CompoundNBT();
                nbtB.putByte(sideB.getName(), tileEntityB.getSideMap().get(sideB).getActualValue());

                PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                        () -> level.getChunkAt(posA)), new SAntlineUpdatePacket(posA, nbtA));
                PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                        () -> level.getChunkAt(posB)), new SAntlineUpdatePacket(posB, nbtB));
            }
        } else {
            if (connect) dotA.addConnection(dirA);
            else dotA.removeConnection(dirA);

            CompoundNBT nbtA = new CompoundNBT();
            nbtA.putByte(sideA.getName(), tileEntityA.getSideMap().get(sideA).getActualValue());

            PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                    () -> level.getChunkAt(posA)), new SAntlineUpdatePacket(posA, nbtA));
        }
    }

    private static void chainPower(World level, BlockPos newPos, Direction dotDirection, Direction powerDirection, boolean active) {
//        ModUtil.sendChat(level, "POWER " + newPos);
        Block block = level.getBlockState(newPos).getBlock();
        if (block instanceof AntlineIndicatorBlock) level.setBlockAndUpdate(newPos, level.getBlockState(newPos).setValue(ACTIVE, active));
        if (block instanceof AntlineActivator
                && ((AntlineActivator) block).getHorsedOn(level.getBlockState(newPos)) == dotDirection
                && ((AntlineActivator) block).isActive(level.getBlockState(newPos))
                && !active) {
            ModUtil.sendChat(level, "PONG " + active); // DEBUG
//            chainPower(level, newPos.relative(powerDirection.getOpposite()), dotDirection, powerDirection.getOpposite(), true);
            return;
        }

        if (!(block instanceof AntlineBlock)) return;

        AntlineTileEntity tileEntity = ((AntlineTileEntity) level.getBlockEntity(newPos));
        AntlineTileEntity.Side side = tileEntity.getSideMap().get(dotDirection);

        if (side.isActive() == active) return; // Prevent loops

        side.setActive(active);

        // Update the model
        level.sendBlockUpdated(newPos, level.getBlockState(newPos), level.getBlockState(newPos), 0);
        tileEntity.requestModelDataUpdate();

        CompoundNBT nbtA = new CompoundNBT();
        nbtA.putByte(dotDirection.getName(), tileEntity.getSideMap().get(dotDirection).getActualValue());

        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                () -> level.getChunkAt(newPos)), new SAntlineUpdatePacket(newPos, nbtA));

//        ModUtil.sendChat(level, "POWER " + side.isActive() + " " + newPos + " " + side.getConnections()); // DEBUG

        modifyConnectedSides(level, newPos, dotDirection, tileEntity, active, true, powerDirection); // From just activated antline
    }

    @Override
    public void neighborChanged(BlockState blockState, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        super.neighborChanged(blockState, level, pos, block, neighborPos, b);

        AntlineTileEntity tileEntity = (AntlineTileEntity) level.getBlockEntity(pos);
        Direction neighborDir = Direction.getNearest(
                neighborPos.getX() - pos.getX(),
                neighborPos.getY() - pos.getY(),
                neighborPos.getZ() - pos.getZ()
        );

        // Connect when adjacent testing element
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == neighborDir.getAxis() || tileEntity.getSideMap().get(direction).isEmpty()) continue;
            modifyConnectedSides(level, pos, direction, tileEntity, true, false, null);
        }

        // Disconnect when no longer connected
//        for (Direction direction : Direction.values()) {
//            if (direction.getAxis() == neighborDir.getAxis() || tileEntity.getSideMap().get(direction).isEmpty()) continue;
//            if (!tileEntity.getSideMap().get(direction).hasConnection(neighborDir)) continue;
//            if (level.getBlockState(neighborPos).getBlock() instanceof AntlineBlock
//                    && ((AntlineTileEntity) level.getBlockEntity(neighborPos)).getSideMap().get(direction).hasConnection(neighborDir.getOpposite())) continue;
//            modifyConnection(level, pos, direction, neighborDir, neighborPos, direction, neighborDir.getOpposite(), false);
//        }

        // Adopt power on/off
        for (Direction direction : Direction.values()) {
            BlockState neighborBlockState = level.getBlockState(neighborPos);
            if (neighborBlockState.getBlock() instanceof AntlineIndicatorBlock) continue;

            AntlineTileEntity.Side side = tileEntity.getSideMap().get(direction);
            if (side.isEmpty() || !side.hasConnection(neighborDir) || neighborDir.getOpposite() == direction) continue;

            boolean active = false;
            if (neighborBlockState.getBlock() instanceof AntlineActivator
                    && ((AntlineActivator) neighborBlockState.getBlock()).getHorsedOn(neighborBlockState) == direction) {
                active = neighborBlockState.getValue(ACTIVE);
            }

            chainPower(level, pos, direction, neighborDir.getOpposite(), active);
        }

        // Break if no supporting block
        for (Direction direction : Direction.values()) {
            if (tileEntity.getSideMap().get(direction).isEmpty()) continue;

            BlockState neighborState = level.getBlockState(pos.relative(direction));

            if (!neighborState.isFaceSturdy(level, pos, direction)) {
                breakDot(blockState, level, pos, null, direction);
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        return context.getItemInHand().getItem() instanceof AntlineBlockItem;
    }

    public void breakDot(BlockState state, World level, BlockPos pos, PlayerEntity player, Direction direction) {
        AntlineTileEntity tileEntity = (AntlineTileEntity)level.getBlockEntity(pos);

        // Break like normal
        if (tileEntity.getSideMap().getSideCount() <= 1) {
            if (!level.isClientSide) AntlineBlock.modifyConnectedSides(level, pos, direction, tileEntity, false, false, null);

            level.destroyBlock(pos, false); //TODO this is causing double sounds when breaking by hand, because the breaking sound is handled somewhere else by minecraft too.
            return;
        }

        if (player != null) level.levelEvent(player, 2001, pos, getId(state));
        tileEntity.getSideMap().put(direction, AntlineTileEntity.Side.empty(direction));

        if (!level.isClientSide) AntlineBlock.modifyConnectedSides(level, pos, direction, tileEntity, false, false, null);

        level.sendBlockUpdated(pos, state, state, 0);
        tileEntity.requestModelDataUpdate();
        if (player == null || !player.isCreative()) dropResources(state, level, pos);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World level, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        Vector3d rayPath = player.getViewVector(0).scale(7);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        for (Direction direction : Direction.values()) {
            BlockRayTraceResult rayHit = getSideShape(level, pos, direction).clip(from, to, pos);

            if (rayHit != null && rayHit.getType() == RayTraceResult.Type.BLOCK) {
                breakDot(state, level, pos, player, direction);
                break;
            }
        }
        
        // TODO use single face shape and subtract all the other faces to remove the intersections
        return false;
    }

    private static final VoxelShape DOT = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 1.0D, 11.0D);
    private static final HashMap<Direction, VoxelShape> externalShapes = new HashMap<>();

    static {
        externalShapes.put(Direction.NORTH, DOT.move(0, 0, -5 / 16f));
        externalShapes.put(Direction.SOUTH, DOT.move(0, 0, 5 / 16f));
        externalShapes.put(Direction.WEST,  DOT.move(-5 / 16f, 0, 0));
        externalShapes.put(Direction.EAST,  DOT.move(5 / 16f, 0, 0));
    }

    private static final HashMap<Direction, Function<VoxelShape, VoxelShape>> ROTATE = new HashMap<>();

    static {
        ROTATE.put(Direction.NORTH, shape -> {
            AxisAlignedBB bb = shape.move(-.5, -.5, -.5).bounds();
            return Block.box(bb.minX * 16, -bb.minZ * 16, bb.minY * 16, bb.maxX * 16, -bb.maxZ * 16, bb.maxY * 16).move(.5, .5, .5);
        });

        ROTATE.put(Direction.SOUTH, shape -> {
            AxisAlignedBB bb = ROTATE.get(Direction.NORTH).apply(shape).move(-.5, -.5, -.5).bounds();
            return Block.box(-bb.minX * 16, bb.minY * 16, -bb.minZ * 16, -bb.maxX * 16, bb.maxY * 16, -bb.maxZ * 16).move(.5, .5, .5);
        });

        ROTATE.put(Direction.EAST, shape -> {
            AxisAlignedBB bb = ROTATE.get(Direction.NORTH).apply(shape).move(-.5, -.5, -.5).bounds();
            return Block.box(-bb.minZ * 16, bb.minY * 16, bb.minX * 16, -bb.maxZ * 16, bb.maxY * 16, bb.maxX * 16).move(.5, .5, .5);
        });

        ROTATE.put(Direction.WEST, shape -> {
            AxisAlignedBB bb = ROTATE.get(Direction.NORTH).apply(shape).move(-.5, -.5, -.5).bounds();
            return Block.box(bb.minZ * 16, bb.minY * 16, -bb.minX * 16, bb.maxZ * 16, bb.maxY * 16, -bb.maxX * 16).move(.5, .5, .5);
        });

        ROTATE.put(Direction.UP, shape -> {
            AxisAlignedBB bb = shape.move(-.5, -.5, -.5).bounds();
            return Block.box(bb.minX * 16, -bb.minY * 16, bb.minZ * 16, bb.maxX * 16, -bb.maxY * 16, bb.maxZ * 16).move(.5, .5, .5);
        });

        ROTATE.put(Direction.DOWN, shape -> shape);
    }

    public VoxelShape getSideShape(IBlockReader level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) == null || ((AntlineTileEntity)level.getBlockEntity(pos)).getSideMap() == null)
            return VoxelShapes.empty();

        AntlineTileEntity.Side sideData = ((AntlineTileEntity)level.getBlockEntity(pos)).getSideMap().get(side);
        AtomicReference<VoxelShape> RESULT = new AtomicReference<>(VoxelShapes.empty());

        if(sideData.getCenter() != FALSE)
            RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(side).apply(DOT)));

        sideData.getConnections().forEach((direction, b) -> {
            if(b) RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(side).apply(externalShapes.get(direction))));
        });

        return RESULT.get();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
//        if (level.getBlockEntity(pos) == null || ((AntlineTileEntity) level.getBlockEntity(pos)).getSideMap() == null)
//            // TODO use proper solution
//            return DOT;
//
        PlayerEntity player = Minecraft.getInstance().player;
        Vector3d rayPath = player.getViewVector(0).scale(7);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        for (Direction direction : Direction.values()) {
            VoxelShape shape = getSideShape(level, pos, direction);
            BlockRayTraceResult rayHit = shape.clip(from, to, pos);

            if (rayHit != null && rayHit.getType() == RayTraceResult.Type.BLOCK)
                return shape;
        }

        // TODO use proper solution

        AtomicReference<VoxelShape> RESULT = new AtomicReference<>(VoxelShapes.empty());

        ((AntlineTileEntity) level.getBlockEntity(pos)).getSideMap().forEach((direction, side) -> {
            if(!side.isEmpty())
                RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(direction).apply(DOT)));
        });

        return RESULT.get();
        // TODO implement actual thing but for now this to compat server
//        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }
}