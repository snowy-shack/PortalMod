package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ClientModUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static net.portalmod.common.sorted.antline.AntlineTileEntity.Side.SideType.NONE;

public class AntlineBlock extends Block {
//    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /** While > 0, {@link #recursiveSignalChain} is on the stack — skip heavy work in {@link #neighborChanged} to avoid setBlock/output feedback loops. */
    private static final ThreadLocal<Integer> SIGNAL_CHAIN_DEPTH = ThreadLocal.withInitial(() -> 0);

    private static final int MAX_RECURSION_DEPTH = 256;
    private static final int NO_THRESHOLD = 20;
    public enum ConnectionType {
        SELF,
        ADJACENT,
        CORNER,
        ELEMENT,
        NONE
    }

    public AntlineBlock(Properties properties) {
        super(properties);
//        registerDefaultState(stateDefinition.any()
//                .setValue(ACTIVE, false));
    }

//    @Override
//    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
//        builder.add(ACTIVE);
//    }

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

    public static ConnectionType getConnectionType(World level, BlockPos pos, Direction sideDir, Direction direction, int threshold) {
        if (!(level.getBlockState(pos).getBlock() instanceof AntlineBlock)) return ConnectionType.NONE;

        BlockState adjacentState = level.getBlockState(pos.relative(direction)); // Adjacent face
        BlockState cornerState   = level.getBlockState(pos.relative(direction).relative(sideDir)); // Face around the corner

        AntlineTileEntity selfEntity = ((AntlineTileEntity) level.getBlockEntity(pos));

        boolean cornerIsSolid = adjacentState.isRedstoneConductor(level, pos.relative(direction));

        if (adjacentState.getBlock() instanceof AntlineBlock) { // Adjacent block
            AntlineTileEntity adjacentEntity = ((AntlineTileEntity) level.getBlockEntity(pos.relative(direction)));

            if (adjacentEntity.getSideMap().hasSide(sideDir)
                    && adjacentEntity.getSideMap().get(sideDir).isConnectableWith(direction.getOpposite())
                    && adjacentEntity.getSideMap().get(sideDir).countConnections() <= threshold)
                return ConnectionType.ADJACENT;
        }

        if (adjacentState.getBlock() instanceof AntlineConnector) { // Adjacent connector
            if (((AntlineConnector) adjacentState.getBlock()).getHorsedOn(adjacentState) == sideDir)
                return ConnectionType.ELEMENT;
        }

        if (!cornerIsSolid && cornerState.getBlock() instanceof AntlineBlock) { // Block around the corner
            AntlineTileEntity cornerEntity = ((AntlineTileEntity) level.getBlockEntity(pos.relative(direction).relative(sideDir)));

            if (cornerEntity.getSideMap().hasSide(direction.getOpposite())
                    && cornerEntity.getSideMap().get(direction.getOpposite()).isConnectableWith(sideDir.getOpposite())
                    && cornerEntity.getSideMap().get(direction.getOpposite()).countConnections() <= threshold)
                return ConnectionType.CORNER;
        }

        if (selfEntity.getSideMap().hasSide(direction)
                && selfEntity.getSideMap().get(direction).isConnectableWith(sideDir)
                && selfEntity.getSideMap().get(direction).countConnections() <= threshold) {
            return ConnectionType.SELF;
        }

        return ConnectionType.NONE;
    }

    public void recursiveSignalChain(World level, AntlineTileEntity.Side side, BlockPos pos, Direction originDirection, boolean active, int depth) {
        SIGNAL_CHAIN_DEPTH.set(SIGNAL_CHAIN_DEPTH.get() + 1);
        try {
            if (depth > MAX_RECURSION_DEPTH) return;
            Boolean newActive = null;

            // Loop prevention
            if (active == side.isActive() && originDirection != null) return;
            side.setActive(active);

            // Dead end
            if (side.countConnections() < 2 && originDirection != null) {
                sendUpdatePacket(level, pos, side.toDirection(), (AntlineTileEntity) level.getBlockEntity(pos));
                return;
            }

            boolean becameActive = active;

            // In all connection directions
            for (Direction connectDirection : side.absoluteConnections()) {
                if (connectDirection.getAxis() == side.toDirection().getAxis()  // Rule out relative up / down
                        || connectDirection == originDirection) continue;       // Prevent signal from going backwards

                // Check direction
                ConnectionType connectionType = getConnectionType(level, pos, side.toDirection(), connectDirection, NO_THRESHOLD);
                if (connectionType == ConnectionType.NONE) continue;

                Friend friend = new Friend(connectionType, pos, side.toDirection(), connectDirection);

                // If it's an element, (un)power it
                if (connectionType == ConnectionType.ELEMENT) {
                    BlockState neighborState = level.getBlockState(friend.pos);
                    Block neighborBlock = neighborState.getBlock();

                    // Power indicators
                    if (neighborBlock instanceof AntlineActivated)
                        ((AntlineActivated) neighborBlock).onAntlineActivation(active, neighborState, level, friend.pos);

                    // Hit another input, adopt its signal
                    if (neighborBlock instanceof AntlineActivator) {
                        newActive = ((AntlineActivator) neighborBlock).isAntlineActive(neighborState);

                        if (newActive && !active) {
                            recursiveSignalChain(level, side, pos, null, newActive, 0);
                        }

                        becameActive = newActive;

                        continue;
                    }
                    continue;
                }

                // It's another Antline. Do a recursive signal call
                AntlineTileEntity entity = ((AntlineTileEntity) level.getBlockEntity(friend.pos));
                AntlineTileEntity.SideMap sideMap = entity.getSideMap();
                recursiveSignalChain(level, sideMap.get(friend.sideDirection), friend.pos, friend.connectDirection, active || becameActive, depth + 1);

                // If the antline we just updated ended up being powered, we set becameActive to true, so we don't unpower the next ones we were supposed to unpower.
                if (((AntlineTileEntity) level.getBlockEntity(friend.pos)).getSideMap().get(friend.sideDirection).isActive()) {
                    becameActive = true;
                }
            }

            sendUpdatePacket(level, pos, side.toDirection(), (AntlineTileEntity) level.getBlockEntity(pos));
        } finally {
            SIGNAL_CHAIN_DEPTH.set(SIGNAL_CHAIN_DEPTH.get() - 1);
        }
    }

    public void sideUpdate(World level, AntlineTileEntity.Side side, BlockPos pos,
                           boolean allowNewConnections, boolean shift, Direction allowedConnectDirection) {
        Boolean active = null;
        Direction originDirection = null;

        // In all directions for that side
        for (Direction connectionDirection : Direction.values()) {
            boolean allowConnection = (allowedConnectDirection == connectionDirection) || allowNewConnections;

            if (!side.isConnectableWith(connectionDirection)                                    // Assert a connection is possible
                    || connectionDirection.getAxis() == side.toDirection().getAxis()) continue; // Rule out relative up / down

            ConnectionType connectionType = getConnectionType(level, pos, side.toDirection(), connectionDirection, (shift || !allowConnection) ? NO_THRESHOLD : 1);

            Friend friend = null;
            if (connectionType != ConnectionType.NONE) friend = new Friend(connectionType, pos, side.toDirection(), connectionDirection);

            boolean element = (connectionType == ConnectionType.ELEMENT);
            boolean connect = (connectionType != ConnectionType.NONE);

            // Make / break the connections
            if (side.hasConnection(connectionDirection) != connect)
                connect(level, pos, side.toDirection(), connectionDirection, connect && allowConnection); // Self

            if (!element && connect && allowConnection)
                connect(level, friend.pos, friend.sideDirection, friend.connectDirection, true); // Neighbor

            if (friend == null) continue;

            BlockState friendState = level.getBlockState(friend.pos);
            if (element && friendState.getBlock() instanceof AntlineActivator) {
                active = ((AntlineActivator) friendState.getBlock()).isAntlineActive(friendState);
                originDirection = connectionDirection;
            }

//            if (connect && !element && friendState.getBlock() instanceof AntlineBlock) {
//                AntlineTileEntity friendEntity = ((AntlineTileEntity) level.getBlockEntity(friend.pos));
//
//                if (friendEntity.getSideMap().get(side.toDirection()).isActive()) {
//                    active = true;
//                    originDirection = connectionDirection;
//                }
//            }
        }

        // If a signal source was found, recurse
        if (active != null) recursiveSignalChain(level, side, pos, originDirection, active, 0);
    }

    private static class Friend {
        public BlockPos  pos = null;
        public Direction sideDirection = null;
        public Direction connectDirection = null;

        /**
         * A connection with a side in a certain direction.
         * @param type type of connection between the two sides.
         * @param pos the original block pos.
         * @param sideDirection the original side direction.
         * @param connectionDirection the direction of the connection from the original side.
         */
        private Friend(ConnectionType type, BlockPos pos, Direction sideDirection, Direction connectionDirection) {
            switch (type) {
                case SELF: {
                    this.pos = pos;
                    this.connectDirection = sideDirection;
                    this.sideDirection = connectionDirection;
                    break;
                }
                case ADJACENT: {
                    this.pos = pos.relative(connectionDirection);
                    this.connectDirection = connectionDirection.getOpposite();
                    this.sideDirection = sideDirection;
                    break;
                }
                case CORNER: {
                    this.pos = pos.relative(connectionDirection).relative(sideDirection);
                    this.connectDirection = sideDirection.getOpposite();
                    this.sideDirection = connectionDirection.getOpposite();
                    break;
                }
                case ELEMENT: {
                    this.pos = pos.relative(connectionDirection);
                    this.sideDirection = sideDirection;
                    this.connectDirection = connectionDirection.getOpposite();
                }
            }
        }
    }

    private void connect(World level, BlockPos pos, Direction side, Direction connectDir, boolean connect) {
        AntlineTileEntity tileEntity = (AntlineTileEntity) level.getBlockEntity(pos);
        AntlineTileEntity.Side sideObj = tileEntity.getSideMap().get(side);

        if (connect)
            sideObj.addConnection(connectDir);
        else
            sideObj.removeConnection(connectDir);

        sendUpdatePacket(level, pos, side, tileEntity);
    }

    @Override
    public void neighborChanged(BlockState blockState, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        super.neighborChanged(blockState, level, pos, block, neighborPos, b);

        if (!level.isClientSide && SIGNAL_CHAIN_DEPTH.get() > 0) {
            return;
        }

        BlockState neighborState = level.getBlockState(neighborPos);

        AntlineTileEntity tileEntity = (AntlineTileEntity) level.getBlockEntity(pos);
        AntlineTileEntity.SideMap sideMap = tileEntity.getSideMap();

        Direction neighborDir = Direction.getNearest(
                neighborPos.getX() - pos.getX(),
                neighborPos.getY() - pos.getY(),
                neighborPos.getZ() - pos.getZ()
        );

        // Break if no supporting block
        if (!sideMap.get(neighborDir).isEmpty()) {
            if (!neighborState.isFaceSturdy(level, pos, neighborDir.getOpposite())) {
                breakDot(blockState, level, pos, null, neighborDir);
                return;
            }
        }

        // Update each side
        for (AntlineTileEntity.Side side : sideMap.values()) {
            if (side.isEmpty()) continue;

            boolean usedToHaveConnection = side.hasConnection(neighborDir);

            // Allow new connections if the updating block is connectable with the side
            sideUpdate(level, side, pos, false, true, neighborState.getBlock() instanceof AntlineConnector ? neighborDir : null);

            boolean hasNowConnection = side.hasConnection(neighborDir);

            if (!usedToHaveConnection) continue;

            //TODO placing a branch next to an antline that is next to an indicator still causes issues

            if (!hasNowConnection) {
                recursiveSignalChain(level, side, pos, null, false, 0);
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        return context.getItemInHand().getItem() instanceof AntlineBlockItem;
    }

    public void breakDot(BlockState state, World level, BlockPos pos, PlayerEntity player, Direction direction) {
        if(level.isClientSide)
            return;

        AntlineTileEntity tileEntity = (AntlineTileEntity) level.getBlockEntity(pos);

        // Break like normal
        if (tileEntity.getSideMap().getSideCount() <= 1) {
            if (player != null) level.levelEvent(null, 2001, pos, getId(state)); // Particles and sound

            if (player == null || !player.isCreative()) dropResources(state, level, pos); // Item drops
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

        } else {
            // Just a dot removed
            level.levelEvent(null, 2001, pos, getId(state)); // Particles and sound
            tileEntity.getSideMap().removeSide(direction);

            if (player == null || !player.isCreative()) dropResources(state, level, pos); // Item drops

            if (level instanceof ServerWorld) {
                // Update each side
                for (AntlineTileEntity.Side side : tileEntity.getSideMap().values()) {
                    if (side.isEmpty() || side.toDirection() == direction) continue;

                    sideUpdate(level, side, pos, false, false, null);
                }

                for (AntlineTileEntity.Side side : tileEntity.getSideMap().values()) {
                    if (side.isEmpty() || side.toDirection() == direction) continue;

                    recursiveSignalChain(level, side, pos, null, false, 0);
                }
            }
        }

        // Update the neighbors of the blocks around the supporting block of each side, for around-the-corner connections
        for (AntlineTileEntity.Side side : tileEntity.getSideMap().values()) {
            level.updateNeighborsAtExceptFromFacing(pos.relative(side.toDirection()), state.getBlock(), side.toDirection().getOpposite());
        }

        level.updateNeighborsAt(pos, state.getBlock());

        sendUpdatePacket(level, pos, direction, tileEntity);
    }

    public void sendUpdatePacket(World level, BlockPos pos, Direction sideDir, AntlineTileEntity tileEntity) {
        if (level.isClientSide) return;
        CompoundNBT nbtA = new CompoundNBT();

        AntlineTileEntity.SideMap sideMap = tileEntity.getSideMap();
        AntlineTileEntity.Side side = sideMap.get(sideDir);

        nbtA.putByte(sideDir.getName(), side.getActualValue());
        PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
                () -> level.getChunkAt(pos)), new SAntlineUpdatePacket(pos, nbtA));
    }

    @Override
    public boolean removedByPlayer(BlockState state, World level, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        Vector3d rayPath = player.getViewVector(0).scale(7);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        for (Direction direction : Direction.values()) {
            BlockRayTraceResult rayHit = getSideShape(level, pos, direction, isHoldingAntline(player)).clip(from, to, pos);

            if (rayHit != null && rayHit.getType() == RayTraceResult.Type.BLOCK) {
                breakDot(state, level, pos, player, direction);
                break;
            }
        }
        return false;
    }

    public static boolean isHoldingAntline(PlayerEntity player) {
        return player.getMainHandItem().getItem() instanceof AntlineBlockItem;
    }

    private static final VoxelShape DOT = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 2.0D, 11.0D);
    private static final VoxelShape WHOLE_SIDE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final HashMap<Direction, VoxelShape> dotShapes = new HashMap<>();

    static {
        dotShapes.put(Direction.NORTH, DOT.move(0, 0, -5 / 16f));
        dotShapes.put(Direction.SOUTH, DOT.move(0, 0, 5 / 16f));
        dotShapes.put(Direction.WEST,  DOT.move(-5 / 16f, 0, 0));
        dotShapes.put(Direction.EAST,  DOT.move(5 / 16f, 0, 0));
    }

    private static final HashMap<Direction, Function<VoxelShape, VoxelShape>> ROTATE = new HashMap<>();

    static { initializeRotateMap(); }

    private static void initializeRotateMap() {
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

    public VoxelShape getSideShape(IBlockReader level, BlockPos pos, Direction sideDirection, boolean largeShape) {
        TileEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || sideDirection == null) return VoxelShapes.empty();

        AntlineTileEntity.SideMap sideMap = ((AntlineTileEntity) blockEntity).getSideMap();
        if (sideMap == null) return VoxelShapes.empty();

        AntlineTileEntity.Side side = sideMap.get(sideDirection);
        AtomicReference<VoxelShape> RESULT = new AtomicReference<>(VoxelShapes.empty());

        if (side.getSideType() != NONE && DOT != null)
            RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(sideDirection).apply(DOT)));

        if (largeShape) {
            if (!side.isEmpty()) {
                RESULT.set(ROTATE.get(sideDirection).apply(WHOLE_SIDE));
            }
        } else {
            side.getConnections().forEach((direction, b) -> {
                if (b && dotShapes.get(direction) != null)
                    RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(sideDirection).apply(dotShapes.get(direction))));
            });
        }

        return RESULT.get();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        if (level.getBlockEntity(pos) == null || ((AntlineTileEntity) level.getBlockEntity(pos)).getSideMap() == null)
            return VoxelShapes.empty();

        AtomicReference<Entity> entity = new AtomicReference<>(context.getEntity());

        if (entity.get() == null) {
            if (!((World)level).isClientSide)
                return VoxelShapes.empty();

            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                entity.set(ClientModUtil.getLocalPlayer());
            });
        }

        Vector3d rayPath = entity.get().getViewVector(0).scale(7);
        Vector3d from = entity.get().getEyePosition(0);
        Vector3d to = from.add(rayPath);

        for (Direction direction : Direction.values()) {
            boolean holdingAntline = entity.get() instanceof PlayerEntity && isHoldingAntline((PlayerEntity) entity.get());
            VoxelShape shape = getSideShape(level, pos, direction, holdingAntline);
            BlockRayTraceResult rayHit = shape.clip(from, to, pos);

            if (rayHit != null && rayHit.getType() == RayTraceResult.Type.BLOCK)
                return shape;
        }

        AtomicReference<VoxelShape> RESULT = new AtomicReference<>(VoxelShapes.empty());

        AntlineTileEntity.SideMap sideMap = ((AntlineTileEntity) level.getBlockEntity(pos)).getSideMap();
        sideMap.forEach((direction, side) -> {
            if(!side.isEmpty())
                RESULT.set(VoxelShapes.or(RESULT.get(), ROTATE.get(direction).apply(DOT)));
        });

        return RESULT.get();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }
}