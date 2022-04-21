//package io.github.serialsniper.portalmod.common.blocks;
//
//import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
//import net.minecraft.block.*;
//import net.minecraft.tileentity.*;
//import net.minecraft.world.*;
//
//public class StencilBoxBlock extends Block {
//    public StencilBoxBlock(Properties properties) {
//        super(properties);
//    }
//
//    @Override
//    public boolean hasTileEntity(BlockState state) {
//        return true;
//    }
//
//    @Override
//    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
//        return TileEntityTypeInit.STENCILBOX.get().create();
//    }
//}