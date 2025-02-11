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
import net.minecraft.world.server.ServerWorld;
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

        // If the targeted block is already an Antline
        if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof AntlineTileEntity) {
            AntlineTileEntity tileEntity = (AntlineTileEntity)level.getBlockEntity(pos);
            AntlineTileEntity.SideMap sideMap = tileEntity.getSideMap();
            Vector3d clickedVector = context.getClickLocation();

            int count = 0;
            if (clickedVector.x == Math.round(clickedVector.x)) count++;
            if (clickedVector.y == Math.round(clickedVector.y)) count++;
            if (clickedVector.z == Math.round(clickedVector.z)) count++;

            if (count != 1 || sideMap.hasSide(clickedFace.getOpposite())) return false;
        } else super.placeBlock(context, state);

        AntlineTileEntity tileEntity = (AntlineTileEntity) level.getBlockEntity(pos);
        AntlineTileEntity.SideMap sideMap = tileEntity.getSideMap();

        sideMap.put(clickedFace.getOpposite(), AntlineTileEntity.Side.dot(clickedFace.getOpposite()));

        if (level instanceof ServerWorld && this.getBlock() instanceof AntlineBlock) {
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
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline", list);
    }
}