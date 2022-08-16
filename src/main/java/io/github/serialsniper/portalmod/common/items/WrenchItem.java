package io.github.serialsniper.portalmod.common.items;

import io.github.serialsniper.portalmod.client.render.ter.FaithPlateTER;
import io.github.serialsniper.portalmod.common.blockentities.FaithPlateTileEntity;
import io.github.serialsniper.portalmod.core.init.PacketInit;
import io.github.serialsniper.portalmod.core.packet.FaithPlateUpdatePacket;
import io.github.serialsniper.portalmod.core.util.RayCaster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        BlockRayTraceResult rayHit = RayCaster.castBlock(player, level, 64);
        Direction face = rayHit.getDirection();
        BlockPos pos = rayHit.getBlockPos();

        if(level.isClientSide && FaithPlateTER.selected != null) {
            TileEntity be = level.getBlockEntity(FaithPlateTER.selected);

            CompoundNBT nbt = new CompoundNBT();
            CompoundNBT target = new CompoundNBT();
            target.putInt("x", pos.getX() - FaithPlateTER.selected.getX());
            target.putInt("y", pos.getY() - FaithPlateTER.selected.getY());
            target.putInt("z", pos.getZ() - FaithPlateTER.selected.getZ());
            target.putByte("side", (byte)face.get3DDataValue());
            target.putFloat("height", Float.NEGATIVE_INFINITY);
            nbt.put("target", target);
            nbt.putBoolean("enabled", true);

            ((FaithPlateTileEntity)be).load(nbt);
            PacketInit.INSTANCE.sendToServer(new FaithPlateUpdatePacket.Server(FaithPlateTER.selected, nbt));
            FaithPlateTER.selected = null;
            return ActionResult.success(player.getItemInHand(hand));
        }

        return super.use(level, player, hand);
    }

    // todo remove this

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return super.useOn(context);
    }
}