package net.portalmod.common.sorted.button;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.blocks.QuadBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.EntityTagInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SuperButtonBlock extends QuadBlock {
    public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<ButtonMode> MODE = EnumProperty.create("mode", ButtonMode.class);
    
    private static final BiHashMap<Direction, QuadBlockCorner, VoxelShapeGroup> SHAPES = new BiHashMap<>();
    
    public SuperButtonBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(CORNER, QuadBlockCorner.UP_LEFT)
                .setValue(PRESSED, false)
                .setValue(ACTIVE, false)
                .setValue(MODE, ButtonMode.NORMAL)
        );
        this.initAABBs();
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, CORNER, PRESSED, ACTIVE, MODE);
    }
    
    @Override
    public void tick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        checkPressed(state, level, pos);
    }
    
    private void initAABBs() {
        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
                .add(0, 0, 0, 12, 3, 12)
                .add(0, 0, 12, 2.5f, 2, 16)
                .add(12, 0, 0, 16, 2, 2.5f)
                .addPart("normal",  0, 3, 0, 8.5f, 5, 8.5f)
                .addPart("pressed", 0, 3, 0, 8.5f, 4, 8.5f)
                .addPart("trigger", 0, 4, 0, 8.5f, 6, 8.5f)
                .build();
        
        for(Direction facing : Direction.values()) {
            for(QuadBlockCorner corner : QuadBlockCorner.values()) {
                Mat4 matrix = Mat4.identity();
                matrix.translate(new Vec3(.5));
                
                if(facing.getAxisDirection() == AxisDirection.NEGATIVE) {
                    matrix.scale(new Vec3(facing.getNormal()).mul(2).add(1));
                    matrix.rotateDeg(new Vec3(facing.getNormal()).to3f(), corner.getRot() - 90);
                } else {
                    matrix.rotateDeg(new Vec3(facing.getNormal()).to3f(), corner.getRot());
                }
                
                if(facing.getAxis() == Axis.X)
                    matrix.rotateDeg(Vector3f.ZP, -90)
                            .rotateDeg(Vector3f.YP, 90);
                
                if(facing.getAxis() == Axis.Z)
                    matrix.rotateDeg(Vector3f.XP, 90);

                matrix.rotateDeg(Vector3f.YP, 90);
                matrix.translate(new Vec3(-.5));
                
                SHAPES.put(facing, corner, shape.clone().transform(matrix));
            }
        }
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {

            // Don't cycle when persistent and active
            boolean shouldCycle = blockState.getValue(MODE) != ButtonMode.PERSISTENT || !blockState.getValue(ACTIVE);

            if (shouldCycle) {
                ButtonMode newMode = this.cycleMode(blockState, world, pos);
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.button_mode." + newMode.getSerializedName()), true);
            }

            this.setBlockStateValue(ACTIVE, false, blockState, world, pos);
            this.checkPressed(blockState, world, pos);

            WrenchItem.playUseSound(world, player);

            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.FAIL;
    }

    public ButtonMode cycleMode(BlockState blockState, World world, BlockPos pos) {
        ButtonMode currentMode = blockState.getValue(MODE);
        ButtonMode newMode = currentMode.cycle();
        this.setBlockStateValue(MODE, newMode, blockState, world, pos);
        return newMode;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
        VoxelShape shape = this.getShape(state);
        return shape != null ? shape : VoxelShapes.empty();
    }
    
    private VoxelShapeGroup getShapeGroup(BlockState state) {
        return SHAPES.get(state.getValue(FACING), state.getValue(CORNER));
    }
    
    private VoxelShape getShape(BlockState state) {
        return this.getShapeGroup(state).getVariant(state.getValue(PRESSED) ? "pressed" : "normal");
    }
    
    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos targetPos, boolean b) {
        if(level.isClientSide)
            return;
        
//        if(!this.isMultiblockComplete(level, pos, state))
//            level.destroyBlock(pos, false, null, 0);
        if(!state.canSurvive(level, pos))
            level.destroyBlock(pos, true, null, 0);

        if (state.getValue(MODE) == ButtonMode.PERSISTENT && state.getValue(ACTIVE)) {
            boolean isPowered = false;
            for (BlockPos checkingPos : getAllPositions(state, pos)) {
                if (level.hasNeighborSignal(checkingPos)) {
                    isPowered = true;
                }
            }
            if (isPowered) {
                this.setBlockStateValue(ACTIVE, false, state, level, pos);
            }
        }
    }
    
    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        if(level.isClientSide)
            return;

        boolean hasScheduledTick = this.getAllPositions(state, pos).stream().anyMatch(pos1 -> level.getBlockTicks().hasScheduledTick(pos1, this));

        if(!hasScheduledTick)
            this.checkPressed(state, level, pos);
    }
    
    private void checkPressed(BlockState state, World level, BlockPos pos) {
        List<BlockPos> blocks = this.getAllBlocks(pos, state.getValue(CORNER), state.getValue(FACING));
        boolean wasPressed = state.getValue(PRESSED);
        boolean wasActive = state.getValue(ACTIVE);
        ButtonMode mode = state.getValue(MODE);

        boolean pressed = blocks.stream().anyMatch(pos1 -> isBeingPressed(state, level, pos1));

        if (pressed) level.getBlockTicks().scheduleTick(pos, this, 10);
        
        if (wasPressed != pressed) {
            this.setBlockStateValue(PRESSED, pressed, state, level, pos);
            playPressSound(level, pos, pressed);

            if (mode == ButtonMode.NORMAL) {
                this.setBlockStateValue(ACTIVE, pressed, state, level, pos);
                playActivationSound(level, pos, pressed);
            }
            else if (mode == ButtonMode.PERSISTENT && !wasActive) {
                this.setBlockStateValue(ACTIVE, true, state, level, pos);
                playActivationSound(level, pos, true);
            }
            else if (mode == ButtonMode.TOGGLE && pressed) {
                this.setBlockStateValue(ACTIVE, !wasActive, state, level, pos);
                playActivationSound(level, pos, !wasActive);
            }
        }
    }

    public static void playPressSound(World level, BlockPos pos, boolean pressed) {
        level.playSound(null, pos, pressed ? SoundInit.SUPER_BUTTON_PRESS.get() : SoundInit.SUPER_BUTTON_RELEASE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
    }

    public static void playActivationSound(World level, BlockPos pos, boolean activated) {
        level.playSound(null, pos, activated ? SoundInit.BUTTON_ACTIVATE.get() : SoundInit.BUTTON_DEACTIVATE.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
    }

    public AxisAlignedBB getTrigger(BlockState state, BlockPos pos) {
        return this.getShapeGroup(state).getPart("trigger").bounds().move(pos);
    }
    
    private boolean isBeingPressed(BlockState state, World level, BlockPos pos) {
        AxisAlignedBB trigger = getTrigger(state, pos);
        
        List<? extends Entity> entities;
        entities = level.getEntities(null, trigger);

        entities.removeIf(entity -> entity.getType().is(EntityTagInit.BUTTON_NO_PRESS));
        return entities.size() > 0;
    }
    
//    private boolean isMultiblockComplete(World level, BlockPos pos, BlockState state) {
//        return checkEachBlock(level, pos, state.getValue(CORNER), state.getValue(FACING),
//                s -> s.getBlock() != BlockInit.SUPER_BUTTON.get());
//    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
       return PushReaction.DESTROY;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("super_button", list);
    }
}