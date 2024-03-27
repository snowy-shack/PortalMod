package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;

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
        if(!canSupportCenter(context.getLevel(),
                context.getClickedPos().relative(context.getClickedFace().getOpposite()),
                context.getClickedFace()))
            return null;
        return super.getStateForPlacement(context);
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos targetPos, boolean b) {
        super.neighborChanged(state, level, pos, block, targetPos, b);

//        if(level.isClientSide()) return;

        AntlineTileEntity blockEntity = (AntlineTileEntity)level.getBlockEntity(pos);

        for(Direction direction : Direction.values()) {
            if(!(pos.relative(direction).getX() == targetPos.getX()
                    && pos.relative(direction).getY() == targetPos.getY()
                    && pos.relative(direction).getZ() == targetPos.getZ()))
                continue;

            if(blockEntity.getSideMap().hasSide(direction)) {
                if(!canSupportCenter(level, targetPos, direction.getOpposite())) {
                    if(blockEntity.getSideMap().getSideCount() <= 1) {
                        dropResources(state, level, pos);
                        level.removeBlock(pos, false);
                    } else {
                        blockEntity.getSideMap().put(direction, new AntlineTileEntity.Side(direction, 0));
                        level.sendBlockUpdated(pos, state, state, 0);

                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putByte(direction.getName(), (byte) 0);

                        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                                () -> level.getChunkAt(pos)), new SAntlineUpdatePacket(pos, nbt));
                    }
                }
            }

            if(level.getBlockState(targetPos).getBlock() == BlockInit.ANTLINE.get()) {
                AntlineTileEntity targetBlockEntity = (AntlineTileEntity)level.getBlockEntity(targetPos);

                for(Direction side : Direction.values()) {
                    if(side.getAxis() == direction.getAxis())
                        continue;

                    AntlineTileEntity.SideMap sideMap = blockEntity.getSideMap();
                    AntlineTileEntity.SideMap targetSideMap = targetBlockEntity.getSideMap();

                    if(sideMap.hasSide(side) && targetSideMap.hasSide(side)
                            && sideMap.get(side).isConnectableWith(direction) && targetSideMap.get(side).isConnectableWith(direction.getOpposite())) {

                        // TODO separate block update

                        sideMap.get(side).addConnection(direction);
                        targetSideMap.get(side).addConnection(direction.getOpposite());

                        level.sendBlockUpdated(pos, state, state, 0);
                        level.sendBlockUpdated(targetPos, state, state, 0);

                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putByte(side.getName(), sideMap.get(side).getValue());

                        CompoundNBT targetNbt = new CompoundNBT();
                        targetNbt.putByte(side.getName(), targetSideMap.get(side).getValue());

                        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                                () -> level.getChunkAt(pos)), new SAntlineUpdatePacket(pos, nbt));

                        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                                () -> level.getChunkAt(targetPos)), new SAntlineUpdatePacket(targetPos, targetNbt));
                    } else if(!targetSideMap.get(side).hasConnection(direction.getOpposite())) {
                        sideMap.get(side).removeConnection(direction);

                        level.sendBlockUpdated(pos, state, state, 0);

                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putByte(side.getName(), sideMap.get(side).getValue());

                        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                                () -> level.getChunkAt(pos)), new SAntlineUpdatePacket(pos, nbt));
                    }
                }
            } else {
                for(Direction side : Direction.values()) {
                    if(side == direction || side == direction.getOpposite())
                        continue;

                    AntlineTileEntity.SideMap sideMap = blockEntity.getSideMap();

                    if(sideMap.get(side).hasConnection(direction)) {
                        sideMap.get(side).removeConnection(direction);

                        for(Direction targetBlockDirection : Direction.values()) {
                            if(targetBlockDirection.getAxis() == side.getAxis()
                                    || level.getBlockState(pos.relative(targetBlockDirection)).getBlock() != BlockInit.ANTLINE.get()
                                    || sideMap.get(side).hasConnection(targetBlockDirection))
                                continue;

                            AntlineTileEntity targetBlockEntity = (AntlineTileEntity)level.getBlockEntity(pos.relative(targetBlockDirection));
                            AntlineTileEntity.SideMap targetSideMap = targetBlockEntity.getSideMap();

                            if(targetSideMap.get(side).isConnectableWith(targetBlockDirection.getOpposite())) {
                                sideMap.get(side).addConnection(targetBlockDirection);
                                targetSideMap.get(side).addConnection(targetBlockDirection.getOpposite());

                                level.sendBlockUpdated(pos.relative(targetBlockDirection), state, state, 0);

                                CompoundNBT nbt = new CompoundNBT();
                                nbt.putByte(side.getName(), targetSideMap.get(side).getValue());

                                PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                                        () -> level.getChunkAt(pos.relative(targetBlockDirection))), new SAntlineUpdatePacket(pos.relative(targetBlockDirection), nbt));
                                break;
                            }
                        }

                        level.sendBlockUpdated(pos, state, state, 0);

                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putByte(side.getName(), sideMap.get(side).getValue());

                        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                                () -> level.getChunkAt(pos)), new SAntlineUpdatePacket(pos, nbt));
                    }
                }
            }
            break;
        }

//        if(!state.canSurvive(level, pos)) {
//            dropResources(state, level, pos);
//            level.removeBlock(pos, false);
//        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        return context.getItemInHand().getItem() == ItemInit.ANTLINE.get();
    }

    @Override
    public boolean removedByPlayer(BlockState state, World level, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        AntlineTileEntity blockEntity = (AntlineTileEntity)level.getBlockEntity(pos);

        if(blockEntity.getSideMap().getSideCount() <= 1)
            return super.removedByPlayer(state, level, pos, player, willHarvest, fluid);

        Vector3d rayPath = player.getViewVector(0).scale(7);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        for(Direction direction : Direction.values()) {
            BlockRayTraceResult rayHit = getSideShape(level, pos, direction).clip(from, to, pos);

            if(rayHit != null && rayHit.getType() == RayTraceResult.Type.BLOCK && blockEntity.getSideMap().getSideCount() > 1) {
                level.levelEvent(player, 2001, pos, getId(state));
                blockEntity.getSideMap().put(direction, AntlineTileEntity.Side.empty(direction));

                for(Direction side : Direction.values()) {
                    if(side.getAxis() == direction.getAxis())
                        continue;

                    if(blockEntity.getSideMap().get(side).hasConnection(direction))
                        blockEntity.getSideMap().get(side).removeConnection(direction);
                }

                level.sendBlockUpdated(pos, state, state, 0);
                level.updateNeighborsAt(pos, BlockInit.ANTLINE.get());
                blockEntity.requestModelDataUpdate();
                if (!player.isCreative()) {
                    dropResources(state, level, pos);
                }
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
        externalShapes.put(Direction.WEST, DOT.move(-5 / 16f, 0, 0));
        externalShapes.put(Direction.EAST, DOT.move(5 / 16f, 0, 0));
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
        if(level.getBlockEntity(pos) == null || ((AntlineTileEntity)level.getBlockEntity(pos)).getSideMap() == null)
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
//        if(level.getBlockEntity(pos) == null || ((AntlineTileEntity)level.getBlockEntity(pos)).getSideMap() == null)
//            // TODO use proper solution
//            return DOT;
//
//        PlayerEntity player = Minecraft.getInstance().player;
//        Vector3d rayPath = player.getViewVector(0).scale(7);
//        Vector3d from = player.getEyePosition(0);
//        Vector3d to = from.add(rayPath);
//
//        for(Direction direction : Direction.values()) {
//            VoxelShape shape = getSideShape(level, pos, direction);
//            BlockRayTraceResult rayHit = shape.clip(from, to, pos);
//
//            if(rayHit != null && rayHit.getType() == RayTraceResult.Type.BLOCK)
//                return shape;
//        }
//
//        // TODO use proper solution
//
//        AtomicReference<VoxelShape> RESULT = new AtomicReference<>(VoxelShapes.empty());
//
//        ((AntlineTileEntity)level.getBlockEntity(pos)).getSideMap().forEach((direction, side) -> {
//            if(!side.isEmpty())
//                RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(direction).apply(DOT)));
//        });
//
//        return RESULT.get();
        // todo implement actual thing but for now this to compat server
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }
}