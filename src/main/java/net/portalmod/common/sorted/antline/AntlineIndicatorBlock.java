package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineIndicatorBlock extends HorizontalFaceBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");
    private static final BiHashMap<Direction, AttachFace, VoxelShapeGroup> SHAPE = new BiHashMap<>();

    public AntlineIndicatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(REVERSED, false)
                .setValue(FACE, AttachFace.FLOOR)
        );
        this.initAABBs();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        boolean reversed = blockState.getValue(REVERSED);
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
            world.setBlockAndUpdate(pos, blockState.setValue(REVERSED, !reversed));
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.indicator_mode." + (reversed ? "normal" : "reversed")), true);

            WrenchItem.playUseSound(world, player);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    public void setActive(boolean active, World world, BlockPos pos) {
        world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ACTIVE, active));
    }

    public static boolean isOn(BlockState blockState) {
        boolean active = blockState.getValue(ACTIVE);
        boolean reversed = blockState.getValue(REVERSED);
        return reversed != active;
    }

//    @Override
//    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
//        super.neighborChanged(state, level, pos, block, neighborPos, b);
//        ModUtil.sendChat(level, "Antline Indicator update");
//        level.sendBlockUpdated(pos, state, state, 0);
//        level.updateNeighborsAt(pos, block);
//    }

    private void initAABBs() {
        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
                .add(0, 3, 3, 2, 13, 13)
                .build();

        for(Direction facing : Direction.values()) {
            for(AttachFace attachFace : AttachFace.values()) {
                Mat4 matrix = Mat4.identity();
                matrix.translate(new Vec3(.5));

                if(attachFace != AttachFace.WALL) {
                    int angle = (attachFace == AttachFace.FLOOR) ? 90 : -90;
                    matrix.rotateDeg(Vector3f.ZP, angle);
                } else {
                    int angle = facing.get2DDataValue() * -90 - 90;
                    matrix.rotateDeg(Vector3f.YP, angle);
                }
                matrix.translate(new Vec3(-.5));

                SHAPE.put(facing, attachFace, shape.clone().transform(matrix));
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(FACING), state.getValue(FACE)).getShape();
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, REVERSED, FACE);
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
       return PushReaction.DESTROY;
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

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_indicator", list);
    }
}