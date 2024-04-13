package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineBlockItem extends BlockItem {
    public AntlineBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        if(level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof AntlineTileEntity) {
            AntlineTileEntity blockEntity = (AntlineTileEntity)level.getBlockEntity(pos);
            AntlineTileEntity.SideMap sideMap = blockEntity.getSideMap();
            Vector3d clickedVector = context.getClickLocation();

            // TODO use ray casting

            int count = 0;
            if(clickedVector.x == Math.round(clickedVector.x)) count++;
            if(clickedVector.y == Math.round(clickedVector.y)) count++;
            if(clickedVector.z == Math.round(clickedVector.z)) count++;

            if(count != 1 || sideMap.hasSide(clickedFace.getOpposite()))
                return false;
        } else {
            super.placeBlock(context, state);
        }

        AntlineTileEntity blockEntity = (AntlineTileEntity)level.getBlockEntity(pos);
        AntlineTileEntity.SideMap sideMap = blockEntity.getSideMap();
        sideMap.put(clickedFace.getOpposite(), AntlineTileEntity.Side.dot(clickedFace.getOpposite()));

        for(Direction side : Direction.values()) {
            if(side.getAxis() == clickedFace.getAxis())
                continue;

            if(sideMap.get(side).isConnectableWith(clickedFace.getOpposite())) {
                sideMap.get(clickedFace.getOpposite()).addConnection(side);
                sideMap.get(side).addConnection(clickedFace.getOpposite());
            }
        }

//        for(Direction targetBlockDirection : Direction.values()) {
//            if(targetBlockDirection.getAxis() == clickedFace.getAxis()
//                    || !sideMap.get(targetBlockDirection).isEmpty()
//                    || level.getBlockState(pos.relative(targetBlockDirection)).getBlock() != BlockInit.ANTLINE.get())
//                continue;
//
//            AntlineTileEntity targetBlockEntity = (AntlineTileEntity)level.getBlockEntity(pos.relative(targetBlockDirection));
//            AntlineTileEntity.SideMap targetSideMap = targetBlockEntity.getSideMap();
//
//            if(targetSideMap.get(clickedFace.getOpposite()).isConnectableWith(targetBlockDirection.getOpposite())) {
//                sideMap.get(clickedFace.getOpposite()).addConnection(targetBlockDirection);
//                targetSideMap.get(clickedFace.getOpposite()).addConnection(targetBlockDirection.getOpposite());
//
//                level.sendBlockUpdated(pos.relative(targetBlockDirection), state, state, 0);
//
//                CompoundNBT nbt = new CompoundNBT();
//                nbt.putByte(clickedFace.getOpposite().getName(), targetSideMap.get(clickedFace.getOpposite()).getValue());
//
//                PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
//                        () -> level.getChunkAt(pos.relative(targetBlockDirection))), new AntlineUpdatePacket.Client(pos.relative(targetBlockDirection), nbt));
//                break;
//            }
//        }

        context.getLevel().sendBlockUpdated(context.getClickedPos(), state, state, 0);
        context.getLevel().updateNeighborsAt(pos, BlockInit.ANTLINE.get());
        blockEntity.requestModelDataUpdate();
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline", list);
    }
}