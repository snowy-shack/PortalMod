package net.portalmod.common.sorted.antline;

import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class AntlineBlockItem extends BlockItem {
    public AntlineBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // If the targeted block is already an Antline
        if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof AntlineTileEntity) {
            AntlineTileEntity tileEntity = (AntlineTileEntity)level.getBlockEntity(pos);
            AntlineTileEntity.SideMap sideMap = tileEntity.getSideMap();
            Vec3 clickedVector = context.getClickLocation();

            int count = 0;
            if (clickedVector.x == Math.round(clickedVector.x)) count++;
            if (clickedVector.y == Math.round(clickedVector.y)) count++;
            if (clickedVector.z == Math.round(clickedVector.z)) count++;

            if (count != 1 || sideMap.hasSide(clickedFace.getOpposite())) return false;
        } else super.placeBlock(context, state);

        AntlineTileEntity tileEntity = (AntlineTileEntity) level.getBlockEntity(pos);
        AntlineTileEntity.SideMap sideMap = tileEntity.getSideMap();

        sideMap.put(clickedFace.getOpposite(), AntlineTileEntity.Side.dot(clickedFace.getOpposite()));

        if (level instanceof ServerLevel && this.getBlock() instanceof AntlineBlock) {
            AntlineBlock block = (AntlineBlock) this.getBlock();

            boolean shift = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
            AntlineTileEntity.Side side = sideMap.get(clickedFace.getOpposite());
            block.sideUpdate(level, side, pos, true, shift, null);

            // Activate if needed
            block.recursiveSignalChain(level, side, pos, null, false, 0);

            block.sendUpdatePacket(level, pos, clickedFace.getOpposite(), (AntlineTileEntity) level.getBlockEntity(pos));
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("antline", list);
    }
}