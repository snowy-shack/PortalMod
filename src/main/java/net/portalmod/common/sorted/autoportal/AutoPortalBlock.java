package net.portalmod.common.sorted.autoportal;

import net.portalmod.common.blocks.OmnidirectionalQuadBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.button.QuadBlockCorner;
import net.portalmod.common.sorted.portal.OrthonormalBasis;
import net.portalmod.common.sorted.portal.PortalColors;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class AutoPortalBlock extends OmnidirectionalQuadBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final BiHashMap<String, QuadBlockCorner, VoxelShapeGroup> SHAPES = new BiHashMap<>();

    public AutoPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(CORNER, QuadBlockCorner.UP_LEFT)
                .setValue(DIRECTION, Direction.NORTH)
                .setValue(POWERED, false)
        );
        this.initAABBs();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CORNER, DIRECTION, POWERED);
    }

    private void initAABBs() {
        VoxelShapeGroup leftShape = new VoxelShapeGroup.Builder()
                .add(3, 0, 0, 6, 16, 1.01)
                .build();

        VoxelShapeGroup rightShape = new VoxelShapeGroup.Builder()
                .add(10, 0, 0, 13, 16, 1.01)
                .build();

        for(Direction direction : Direction.values()) {
            if(direction.getAxis() == Direction.Axis.Y)
                continue;

            for (Direction facing : Direction.values()) {
                for (QuadBlockCorner corner : QuadBlockCorner.values()) {
                    Tuple<Direction, Direction> directions = placementDirectionsFromFacingAndDirection(facing, direction);
                    Direction a = directions.getA();
                    Direction b = directions.getB();
                    int x = QuadBlockCorner.DOWN_RIGHT.getX() - QuadBlockCorner.DOWN_LEFT.getX();
                    int y = QuadBlockCorner.UP_LEFT.getY() - QuadBlockCorner.DOWN_LEFT.getY();

                    if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                        x *= -1;
                    if (x < 0)
                        a = a.getOpposite();
                    if (y < 0)
                        b = b.getOpposite();

                    Vec3 up = new Vec3(b);
                    Vec3 right = new Vec3(a);
                    Mat4 matrix = new OrthonormalBasis(right, up).getChangeOfBasisFromCanonicalMatrix();

                    Mat4 am = Mat4.identity()
                            .translate(.5, .5, .5)
                            .mul(matrix)
                            .translate(-.5, -.5, -.5);

                    SHAPES.put(facing + " " + direction, corner, (corner.isLeft() ? leftShape : rightShape).clone().transform(am));
                }
            }
        }
    }

    public void setAntlinePowered(boolean powered, BlockState blockState, Level world, BlockPos pos) {
        this.setBlockStateValue(POWERED, powered, blockState, world, pos);
        this.updateAllNeighbors(world, pos, blockState);
    }

    private VoxelShapeGroup getShapeGroup(BlockState state) {
        return SHAPES.get(state.getValue(FACING) + " " + state.getValue(DIRECTION), state.getValue(CORNER));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if(!WrenchItem.usedWrench(player, hand))
            return InteractionResult.PASS;

        if(level.isClientSide)
            return InteractionResult.CONSUME;

        Block block = state.getBlock();
        if(!(block instanceof AutoPortalBlock))
            return InteractionResult.PASS;

        BlockPos tePos = ((AutoPortalBlock)block).getOtherBlock(pos, state.getValue(CORNER), QuadBlockCorner.DOWN_LEFT,
                state.getValue(FACING), state.getValue(DIRECTION));

        BlockEntity te = level.getBlockEntity(tePos);
        if(!(te instanceof AutoPortalTileEntity))
            return InteractionResult.PASS;

        AutoPortalTileEntity autoPortal = (AutoPortalTileEntity)te;

        if(player.getOffhandItem().getItem() instanceof PortalGun) {
            ItemStack itemStack = player.getOffhandItem();
            Optional<UUID> uuid = PortalGun.getUUID(itemStack);

            if(!uuid.isPresent() || !itemStack.hasTag())
                return InteractionResult.PASS;

            CompoundTag nbt = itemStack.getTag();

            if(nbt != null) {
                if(!nbt.contains("LeftColor") || !nbt.contains("RightColor"))
                    return InteractionResult.PASS;

                int primaryColor = PortalColors.getIndex(nbt.getString("LeftColor"));
                int secondaryColor = PortalColors.getIndex(nbt.getString("RightColor"));
                PortalEnd end = nbt.contains("Locked") && nbt.getString("Locked").equals("Left")
                        ? PortalEnd.PRIMARY : PortalEnd.SECONDARY;

                autoPortal.link(uuid.get(), end, primaryColor, secondaryColor);
                WrenchItem.playUseSound(level, rayTraceResult.getLocation());
                player.displayClientMessage(new TranslatableComponent("actionbar.portalmod.autoportal.set"), true);
                return InteractionResult.SUCCESS;
            }
        } else {
            if(autoPortal.end == null) {
                WrenchItem.playFailSound(level, rayTraceResult.getLocation());
                return InteractionResult.SUCCESS;
            }

            if(autoPortal.lastOpenedUUID != null) {
                autoPortal.closePortal();
                WrenchItem.playUseSound(level, rayTraceResult.getLocation());
                return InteractionResult.SUCCESS;
            }

            autoPortal.swapEnd();
            WrenchItem.playUseSound(level, rayTraceResult.getLocation());

            player.displayClientMessage(new TranslatableComponent("actionbar.portalmod.autoportal."
                    + (autoPortal.end == PortalEnd.PRIMARY ? "primary" : "secondary")), true);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = this.getShapeGroup(state).getShape();
        return shape != null ? shape : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public boolean isCornerPlaceable(BlockPlaceContext context, QuadBlockCorner corner) {
        Direction direction = context.getClickedFace();
        return this.getAllBlocks(context.getClickedPos(), corner, direction, context.getHorizontalDirection()).stream()
                .allMatch(pos ->
                        ModUtil.canPlaceAt(context, pos) && context.getLevel()
                                .getBlockState(pos.relative(direction.getOpposite()))
                                .isFaceSturdy(context.getLevel(), pos, direction)
                );
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        if(level.isClientSide)
            return;

        Direction facing = state.getValue(FACING);

        // Reset when powered from behind
        boolean isPowered = getAllPositions(state, pos).stream()
                .filter(blockPos -> level.getBlockState(blockPos).getBlock() instanceof AutoPortalBlock)
                .anyMatch(checkingPos -> level.hasSignal(checkingPos.relative(facing.getOpposite()), facing));

        BlockEntity te = level.getBlockEntity(getOtherBlock(pos, state.getValue(CORNER), QuadBlockCorner.DOWN_LEFT, facing, state.getValue(DIRECTION)));

        if(te instanceof AutoPortalTileEntity) {
            ((AutoPortalTileEntity) te).setPowered(isPowered);
        }

        if(!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true, null, 0);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(CORNER) == QuadBlockCorner.DOWN_LEFT;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return TileEntityTypeInit.AUTOPORTAL.get().create();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("autoportal", list);
    }
}