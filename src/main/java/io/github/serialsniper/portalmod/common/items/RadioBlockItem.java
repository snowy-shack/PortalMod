package io.github.serialsniper.portalmod.common.items;

import io.github.serialsniper.portalmod.common.blockentities.RadioBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;

public class RadioBlockItem extends BlockItem {
    public RadioBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        boolean b = super.placeBlock(context, state);
        if(b && !context.getLevel().isClientSide()) {
            RadioBlockTileEntity radio = ((RadioBlockTileEntity)context.getLevel().getBlockEntity(context.getClickedPos()));
            radio.setInitialized();
            radio.sendUpdatePacket();
        }
        return b;
    }
}