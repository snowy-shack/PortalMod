package net.portalmod.common.sorted.radio;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;

public class RadioBlockItem extends BlockItem {
    public RadioBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean b = super.placeBlock(context, state);
        if(b && !context.getLevel().isClientSide()) {
            RadioBlockTileEntity radio = ((RadioBlockTileEntity)context.getLevel().getBlockEntity(context.getClickedPos()));
            radio.setInitialized();
            radio.sendUpdatePacket();
        }
        return b;
    }
}