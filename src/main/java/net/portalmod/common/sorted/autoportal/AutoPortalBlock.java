package net.portalmod.common.sorted.autoportal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
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
import java.util.*;

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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
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

    public void setPowered(boolean powered, BlockState blockState, World world, BlockPos pos) {
        this.setBlockStateValue(POWERED, powered, blockState, world, pos);
        this.updateAllNeighbors(world, pos, blockState);
    }

    private VoxelShapeGroup getShapeGroup(BlockState state) {
        return SHAPES.get(state.getValue(FACING) + " " + state.getValue(DIRECTION), state.getValue(CORNER));
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(level.isClientSide)
            return ActionResultType.PASS;

        Block block = state.getBlock();
        if(!(block instanceof AutoPortalBlock))
            return ActionResultType.PASS;

        BlockPos tePos = ((AutoPortalBlock)block).getOtherBlock(pos, state.getValue(CORNER), QuadBlockCorner.DOWN_LEFT,
                state.getValue(FACING), state.getValue(DIRECTION));

        TileEntity te = level.getBlockEntity(tePos);
        if(!(te instanceof AutoPortalTileEntity))
            return ActionResultType.PASS;

        AutoPortalTileEntity autoPortal = (AutoPortalTileEntity)te;

        if(WrenchItem.usedWrench(player, hand)) {
            if(player.getOffhandItem().getItem() instanceof PortalGun) {
                ItemStack itemStack = player.getOffhandItem();
                Optional<UUID> uuid = PortalGun.getUUID(itemStack);

                if(uuid.isPresent() && itemStack.hasTag()) {
                    CompoundNBT nbt = itemStack.getTag();

                    if(nbt != null) {
                        if(!nbt.contains("LeftColor") || !nbt.contains("RightColor"))
                            return ActionResultType.PASS;

                        int primaryColor = PortalColors.getIndex(nbt.getString("LeftColor"));
                        int secondaryColor = PortalColors.getIndex(nbt.getString("RightColor"));
                        PortalEnd end = nbt.contains("Locked") && nbt.getString("Locked").equals("Left")
                                ? PortalEnd.PRIMARY : PortalEnd.SECONDARY;

                        autoPortal.link(uuid.get(), end, primaryColor, secondaryColor);
                        WrenchItem.playUseSound(level, rayTraceResult.getLocation());
                        player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.autoportal.set"), true);
                        return ActionResultType.SUCCESS;
                    }
                }
            } else {
                if(autoPortal.end == null) {
                    WrenchItem.playFailSound(level, rayTraceResult.getLocation());
                    return ActionResultType.SUCCESS;
                }

                autoPortal.swapEnd();
                WrenchItem.playUseSound(level, rayTraceResult.getLocation());

                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.autoportal."
                        + (autoPortal.end == PortalEnd.PRIMARY ? "primary" : "secondary")), true);

                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos) {
        QuadBlockCorner corner = state.getValue(CORNER);
        Direction facing = state.getValue(FACING);
        Direction direction = state.getValue(DIRECTION);

        return this.getAllBlocks(pos, corner, facing, direction).stream()
                .allMatch(otherPos -> level.getBlockState(otherPos.relative(facing.getOpposite()))
                        .isFaceSturdy(level, otherPos, facing));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = this.getShapeGroup(state).getShape();
        return shape != null ? shape : VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        if(level.isClientSide)
            return;

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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypeInit.AUTOPORTAL.get().create();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("autoportal", list);
    }
}