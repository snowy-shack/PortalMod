package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.common.sorted.portal.PortalPair;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunAnimation;
import net.portalmod.common.sorted.portalgun.SPortalGunAnimationPacket;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FizzlerEmitterBlock extends DoubleBlock {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public FizzlerEmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
        this.initAABBs();
    }

    private static final Map<Direction, VoxelShapeGroup> SHAPE = new HashMap<>();
    private static final VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
            .add(0, 0, 2, 1, 16, 14)
            .addPart("activeCollision", 1, 0, 5, 4, 16, 11)
            .addPart("active", 1, 0, 5, 4, 16, 11)
            .addPart("active",0,0,7,16,16,9)
            .build();

    private void initAABBs() {
        for(Direction facing : Direction.values()) {
            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));

            int angle = facing.get2DDataValue() * -90 - 90;
            matrix.rotateDeg(Vector3f.YP, angle);

            matrix.translate(new Vec3(-.5));

            SHAPE.put(facing, shape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(FACING)).getVariant(state.getValue(ACTIVE) ? "active" : "");
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(FACING)).getVariant(state.getValue(ACTIVE) ? "activeCollision" : "");
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        if (entity instanceof Cube) {
            ((Cube) entity).startFizzling();
        }

        if(level.isClientSide)
            return;

        if (entity instanceof PlayerEntity) {
            boolean didFizzleAny = false;

            for (ItemStack itemStack : ((PlayerEntity) entity).inventory.items) {
                if (!(itemStack.getItem() instanceof PortalGun))
                    continue;

                UUID gunUUID = PortalGun.getUUID(itemStack);
                PortalPair pair = PortalManager.getPair(gunUUID);

                if (pair == null)
                    continue;

                if (pair.has(PortalEnd.BLUE)) {
                    PortalEntity blue = pair.get(PortalEnd.BLUE);
                    ((ServerWorld) blue.level).removeEntity(blue, false);
                    PortalManager.remove(gunUUID, blue);
                    didFizzleAny = true;
                }
                if (pair.has(PortalEnd.ORANGE)) {
                    PortalEntity orange = pair.get(PortalEnd.ORANGE);
                    ((ServerWorld) orange.level).removeEntity(orange, false);
                    PortalManager.remove(gunUUID, orange);
                    didFizzleAny = true;
                }
            }

            if (didFizzleAny)
                PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity),
                        new SPortalGunAnimationPacket(UUID.randomUUID(), PortalGunAnimation.FIZZLE));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos clickedPos = context.getClickedPos();
        World world = context.getLevel();

        if (world.getBlockState(clickedPos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getClickedFace());
        }
        else if (world.getBlockState(clickedPos.below()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getClickedFace())
                    .setValue(HALF, DoubleBlockHalf.UPPER);
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, HALF);
    }
}
