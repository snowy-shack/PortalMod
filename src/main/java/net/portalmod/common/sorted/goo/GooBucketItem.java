package net.portalmod.common.sorted.goo;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.portalmod.core.init.SoundInit;

import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class GooBucketItem extends BucketItem {
    public GooBucketItem(Fluid fluid, Properties builder) {
        super(fluid, builder);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("goo_bucket", list);
    }

    @Override
    protected void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos) {
        level.playSound(player, pos, SoundInit.BUCKET_EMPTY_GOO.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
