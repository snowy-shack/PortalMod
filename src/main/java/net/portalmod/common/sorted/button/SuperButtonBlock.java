package net.portalmod.common.sorted.button;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
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
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class SuperButtonBlock extends MultiBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<QuadBlockCorner> CORNER = EnumProperty.create("corner", QuadBlockCorner.class);
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
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        QuadBlockCorner corner = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);

        Direction horizontal = facing == Direction.UP ? Direction.WEST : facing == Direction.DOWN ? Direction.EAST : facing.getClockWise();
        Direction vertical = facing.getAxis() == Axis.Y ? Direction.NORTH : Direction.UP;

        if (!corner.isLeft()) {
            pos = pos.relative(horizontal);
        }
        if (!corner.isUp()) {
            pos = pos.relative(vertical);
        }
        return pos;
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        Direction facing = blockState.getValue(FACING);

        Direction horizontal = facing == Direction.UP ? Direction.EAST : facing == Direction.DOWN ? Direction.WEST : facing.getCounterClockWise();
        Direction vertical = facing.getAxis() == Axis.Y ? Direction.SOUTH : Direction.DOWN;

        return new ArrayList<>(Arrays.asList(
                mainPos.relative(horizontal),
                mainPos.relative(horizontal).relative(vertical),
                mainPos.relative(vertical)
        ));
    }

    @Override
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
        QuadBlockCorner base = blockState.getValue(CORNER);
        Direction facing = blockState.getValue(FACING);

        for(QuadBlockCorner corner : QuadBlockCorner.values()) {
            if(corner == base)
                continue;
            world.setBlock(getOtherBlock(pos, base, corner, facing), blockState.setValue(CORNER, corner), BlockFlags.DEFAULT);
        }
    }

    @Override
    public boolean isMainBlock(BlockState blockState) {
        return blockState.getValue(CORNER) == QuadBlockCorner.UP_LEFT;
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
    
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Vec3 playerView = new Vec3(context.getPlayer().getViewVector(1));
        Direction direction = context.getClickedFace();
        Axis axis = direction.getAxis();
        QuadBlockCorner corner;
        
        Tuple<Direction, Direction> directions = placementDirectionsFromFacing(axis);
        Direction a = directions.getA();
        Direction b = directions.getB();
        
        if(direction.getAxisDirection() == AxisDirection.NEGATIVE) {
            if(direction.getAxis() == Axis.X)
                playerView.z *= -1;
            else
                playerView.x *= -1;
        }
        
        double x = a.getAxisDirection().getStep() * -playerView.to3d().get(a.getAxis());
        double y = b.getAxisDirection().getStep() * -playerView.to3d().get(b.getAxis());
        corner = QuadBlockCorner.fromCoords(x, y);
//        System.out.println(x);
//        System.out.println(y);
        
        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(CORNER, corner);

        boolean xNeg = false;
        boolean yNeg = false;

        if(x < y)
            xNeg = true;
        else
            yNeg = true;

        corner = QuadBlockCorner.fromCoords(x * (xNeg ? -1 : 1), y * (yNeg ? -1 : 1));

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);

        corner = QuadBlockCorner.fromCoords(x * (xNeg ? 1 : -1), y * (yNeg ? 1 : -1));

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);

        corner = QuadBlockCorner.fromCoords(-x, -y);

        if(this.isPlaceable(context, corner))
            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(CORNER, corner);
        
        return null;
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
            ButtonMode newMode = this.cycleMode(blockState, world, pos);
            this.setBlockStateValue(ACTIVE, false, blockState, world, pos);
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.button_mode." + newMode.getSerializedName()), true);
            this.checkPressed(blockState, world, pos);
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

    private boolean canSurvive(IWorldReader level, BlockPos pos, QuadBlockCorner corner, Direction facing) {
        for(BlockPos targetPos : this.getAllBlocks(pos, corner, facing))
            if(!canSupportCenter(level, targetPos.relative(facing.getOpposite()), facing))
                return false;
        return true;
    }
    
    @Override
    public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos) {
        return canSurvive(level, pos, state.getValue(CORNER), state.getValue(FACING));
    }

    private Tuple<Direction, Direction> placementDirectionsFromFacing(Axis axis) {
        if(axis == Axis.X)
            return new Tuple<>(Direction.NORTH, Direction.UP);
        if(axis == Axis.Z)
            return new Tuple<>(Direction.EAST, Direction.UP);
        return new Tuple<>(Direction.EAST, Direction.NORTH);
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

        boolean hasScheduledTick = false;
        for (BlockPos blockPos : this.getAllPositions(state, pos)) {
            if (level.getBlockTicks().hasScheduledTick(blockPos, this)) {
                hasScheduledTick = true;
                break;
            }
        }

        if(!hasScheduledTick)
            this.checkPressed(state, level, pos);
    }
    
    private void checkPressed(BlockState state, World level, BlockPos pos) {
        List<BlockPos> blocks = this.getAllBlocks(pos, state.getValue(CORNER), state.getValue(FACING));
        boolean wasPressed = state.getValue(PRESSED);
        boolean wasActive = state.getValue(ACTIVE);
        ButtonMode mode = state.getValue(MODE);

        boolean pressed = false;
        for(BlockPos block : blocks) {
            if(isBeingPressed(state, level, block)) {
                pressed = true;
                break;
            }
        }

        if(pressed)
            level.getBlockTicks().scheduleTick(pos, this, 12);
        
        if(wasPressed != pressed) {
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
        level.playSound(null, pos, pressed ? SoundInit.SUPER_BUTTON_PRESS.get() : SoundInit.SUPER_BUTTON_RELEASE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    public static void playActivationSound(World level, BlockPos pos, boolean activated) {
        level.playSound(null, pos, activated ? SoundInit.SUPER_BUTTON_ACTIVATE.get() : SoundInit.SUPER_BUTTON_DEACTIVATE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    public AxisAlignedBB getTrigger(BlockState state, BlockPos pos) {
        return this.getShapeGroup(state).getPart("trigger").bounds().move(pos);
    }
    
    private boolean isBeingPressed(BlockState state, World level, BlockPos pos) {
        AxisAlignedBB trigger = getTrigger(state, pos);
        
        List<? extends Entity> entities;
        entities = level.getEntities(null, trigger);
        
//        for(Entity entity : entities)
////            if(!entity.isIgnoringBlockTriggers())
//            if(entity instanceof Cube)
//                ((Cube)entity).setActive();

        return entities.size() > 0;
    }
    
    private boolean isPlaceable(BlockItemUseContext context, QuadBlockCorner corner) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return checkEachBlock(level, pos, corner, context.getClickedFace(), s -> !s.canBeReplaced(context))
                && canSurvive(level, pos, corner, context.getClickedFace());
    }
    
//    private boolean isMultiblockComplete(World level, BlockPos pos, BlockState state) {
//        return checkEachBlock(level, pos, state.getValue(CORNER), state.getValue(FACING),
//                s -> s.getBlock() != BlockInit.SUPER_BUTTON.get());
//    }

    private boolean checkEachBlock(IWorldReader level, BlockPos pos, QuadBlockCorner corner, Direction facing, Predicate<BlockState> p) {
        for(BlockPos targetPos : this.getAllBlocks(pos, corner, facing))
            if(p.test(level.getBlockState(targetPos)))
                return false;
        return true;
    }
    
    private List<BlockPos> getAllBlocks(BlockPos pos, QuadBlockCorner base, Direction facing) {
        List<BlockPos> poses = new ArrayList<>();
        for(QuadBlockCorner corner : QuadBlockCorner.values())
            poses.add(getOtherBlock(pos, base, corner, facing));
        return poses;
    }
    
    private BlockPos getOtherBlock(BlockPos pos, QuadBlockCorner base, QuadBlockCorner corner, Direction facing) {
        Tuple<Direction, Direction> directions = placementDirectionsFromFacing(facing.getAxis());
        Direction a = directions.getA();
        Direction b = directions.getB();
        int x = corner.getX() - base.getX();
        int y = corner.getY() - base.getY();
        
        if(facing.getAxisDirection() == AxisDirection.NEGATIVE)
            x *= -1;
        
        BlockPos newPos = new BlockPos(pos);
        if(x != 0) newPos = newPos.relative(x < 0 ? a.getOpposite() : a);
        if(y != 0) newPos = newPos.relative(y < 0 ? b.getOpposite() : b);
        
        return newPos;
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
       return PushReaction.DESTROY;
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("super_button", list);
    }
}