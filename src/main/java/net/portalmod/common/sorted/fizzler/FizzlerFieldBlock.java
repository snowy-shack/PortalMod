package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.common.sorted.portal.PortalPair;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunAnimation;
import net.portalmod.common.sorted.portalgun.SPortalGunAnimationPacket;
import net.portalmod.core.HorizontalAxis;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.PacketInit;

import java.util.UUID;

public class FizzlerFieldBlock extends Block {
    public static final EnumProperty<HorizontalAxis> AXIS = EnumProperty.create("axis", HorizontalAxis.class);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public FizzlerFieldBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, HorizontalAxis.X)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, HALF);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        if(!(entity instanceof PlayerEntity) || level.isClientSide)
            return;

        boolean didFizzleAny = false;

        for(ItemStack itemStack : ((PlayerEntity)entity).inventory.items) {
            if(!(itemStack.getItem() instanceof PortalGun))
                continue;

            UUID gunUUID = PortalGun.getUUID(itemStack);
            PortalPair pair = PortalManager.getPair(gunUUID);

            if(pair == null)
                continue;

            if(pair.has(PortalEnd.BLUE)) {
                PortalEntity blue = pair.get(PortalEnd.BLUE);
                ((ServerWorld)blue.level).removeEntity(blue, false);
                PortalManager.remove(gunUUID, blue);
                didFizzleAny = true;
            }
            if(pair.has(PortalEnd.ORANGE)) {
                PortalEntity orange = pair.get(PortalEnd.ORANGE);
                ((ServerWorld)orange.level).removeEntity(orange, false);
                PortalManager.remove(gunUUID, orange);
                didFizzleAny = true;
            }
        }

        if(didFizzleAny)
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)entity),
                    new SPortalGunAnimationPacket(UUID.randomUUID(), PortalGunAnimation.FIZZLE));
    }
}