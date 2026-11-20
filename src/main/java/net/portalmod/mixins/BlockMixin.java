package net.portalmod.mixins;

import net.minecraftforge.common.IPlantable;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.panel.PanelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "canSustainPlant", at = @At("HEAD"), cancellable = true, remap=false)
    private void allowArboredPlains(BlockState state, BlockGetter world, BlockPos pos,
                                    Direction facing, IPlantable plantable, CallbackInfoReturnable<Boolean> cir) {

        if (plantable.getPlantType(world, pos.relative(facing)) == net.minecraftforge.common.PlantType.PLAINS) {
            ResourceLocation registryName = state.getBlock().getRegistryName();
            if (registryName != null && state.getBlock() instanceof PanelBlock && registryName.getPath().startsWith("arbored")) {
                cir.setReturnValue(state.isFaceSturdy(world, pos.below(), Direction.UP));
            }
        }
    }
}