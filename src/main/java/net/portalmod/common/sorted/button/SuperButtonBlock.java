package net.portalmod.common.sorted.button;

import net.portalmod.common.blocks.QuadBlock;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivator;
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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class SuperButtonBlock extends QuadBlock implements AntlineActivator {
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
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
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
                    matrix.rotateDeg(com.mojang.math.Axis.ZP, -90)
                            .rotateDeg(com.mojang.math.Axis.YP, 90);
                
                if(facing.getAxis() == Axis.Z)
                    matrix.rotateDeg(com.mojang.math.Axis.XP, 90);

                matrix.rotateDeg(com.mojang.math.Axis.YP, 90);
                matrix.translate(new Vec3(-.5));
                
                SHAPES.put(facing, corner, shape.clone().transform(matrix));
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {

            // Don't cycle when persistent/toggle and active
            boolean shouldCycle = blockState.getValue(MODE) == ButtonMode.NORMAL || !blockState.getValue(ACTIVE);

            if (shouldCycle) {
                ButtonMode newMode = this.cycleMode(blockState, world, pos);
                player.displayClientMessage(Component.translatable("actionbar.portalmod.button_mode." + newMode.getSerializedName()), true);
            }

            this.setBlockStateValue(ACTIVE, false, blockState, world, pos);
            this.checkPressed(blockState, world, pos);

            WrenchItem.playUseSound(world, result.getLocation());

            this.updateAllNeighbors(world, pos, blockState);

            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public ButtonMode cycleMode(BlockState blockState, Level world, BlockPos pos) {
        ButtonMode currentMode = blockState.getValue(MODE);
        ButtonMode newMode = currentMode.cycle();
        this.setBlockStateValue(MODE, newMode, blockState, world, pos);
        return newMode;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext selectionContext) {
        VoxelShape shape = this.getShapeGroup(state).getVariant(state.getValue(PRESSED) ? "pressed" : "normal");
        return shape != null ? shape : Shapes.empty();
    }
    
    private VoxelShapeGroup getShapeGroup(BlockState state) {
        return SHAPES.get(state.getValue(FACING), state.getValue(CORNER));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        if (level.isClientSide) return;

        if(!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true, null, 0);
        }

        Direction facing = state.getValue(FACING);

        // Reset when powered from "below"
        boolean isPowered = getAllPositions(state, pos).stream()
                .filter(blockPos -> level.getBlockState(blockPos).getBlock() instanceof SuperButtonBlock)
                .anyMatch(checkingPos -> level.hasSignal(checkingPos.relative(facing.getOpposite()), facing));

        if (isPowered && state.getValue(ACTIVE)) {
            this.setBlockStateValue(ACTIVE, false, state, level, pos);
            this.updateAllNeighbors(level, pos, state);
            this.playPressSound(level, pos, false);
        }
    }
    
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if(level.isClientSide)
            return;

        boolean hasScheduledTick = this.getAllPositions(state, pos).stream().anyMatch(pos1 -> level.getBlockTicks().hasScheduledTick(pos1, this));

        if(!hasScheduledTick)
            this.checkPressed(state, level, pos);
    }
    
    private void checkPressed(BlockState state, Level level, BlockPos pos) {
        List<BlockPos> blocks = this.getAllPositions(state, pos);
        boolean wasPressed = state.getValue(PRESSED);
        boolean wasActive = state.getValue(ACTIVE);
        ButtonMode mode = state.getValue(MODE);

        boolean pressed = blocks.stream().anyMatch(cornerPos -> this.isBeingPressed(level, cornerPos));

        if (pressed) level.getBlockTicks().scheduleTick(pos, this, 10);
        
        if (wasPressed != pressed) {
            this.setBlockStateValue(PRESSED, pressed, state, level, pos);
            this.playPressSound(level, pos, pressed);

            if (mode == ButtonMode.NORMAL) {
                this.setBlockStateValue(ACTIVE, pressed, state, level, pos);
                this.playActivationSound(level, pos, pressed);
            }
            else if (mode == ButtonMode.PERSISTENT && !wasActive) {
                this.setBlockStateValue(ACTIVE, true, state, level, pos);
                this.playActivationSound(level, pos, true);
            }
            else if (mode == ButtonMode.TOGGLE && pressed) {
                this.setBlockStateValue(ACTIVE, !wasActive, state, level, pos);
                this.playActivationSound(level, pos, !wasActive);
            }

            this.updateAllNeighbors(level, pos, state);
        }
    }

    public void playPressSound(Level level, BlockPos pos, boolean pressed) {
        level.playSound(null, pos, pressed ? SoundInit.SUPER_BUTTON_PRESS.get() : SoundInit.SUPER_BUTTON_RELEASE.get(), SoundSource.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
    }

    public void playActivationSound(Level level, BlockPos pos, boolean activated) {
        level.playSound(null, pos, activated ? SoundInit.BUTTON_ACTIVATE.get() : SoundInit.BUTTON_DEACTIVATE.get(), SoundSource.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
    }

    public AABB getTrigger(BlockState state, BlockPos pos) {
        return this.getShapeGroup(state).getPart("trigger").bounds().move(pos);
    }
    
    private boolean isBeingPressed(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SuperButtonBlock)) return false;
        AABB trigger = this.getTrigger(state, pos);

        List<? extends Entity> entities = level.getEntitiesOfClass(LivingEntity.class, trigger, EntitySelector.NO_SPECTATORS
                .and(entity -> !entity.getType().is(EntityTagInit.BUTTON_NO_PRESS))
                .and(entity -> !(entity instanceof TestElementEntity && ((TestElementEntity) entity).isFizzling())));

        return !entities.isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("super_button", list);
    }

    @Override
    public boolean isAntlineActive(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    public Direction getHorsedOn(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    @Override
    public boolean antlineConnectsInDirection(Direction direction, BlockState state) {
        return direction.getAxis() != state.getValue(FACING).getAxis();
    }
}