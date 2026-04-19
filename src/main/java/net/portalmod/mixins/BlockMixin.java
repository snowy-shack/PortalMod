package net.portalmod.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.IPlantable;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.panel.PanelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "canSustainPlant", at = @At("HEAD"), cancellable = true, remap = false)
    private void allowArboredPlains(BlockState state, IBlockReader world, BlockPos pos,
                                    Direction facing, IPlantable plantable, CallbackInfoReturnable<Boolean> cir) {

        if (plantable.getPlantType(world, pos.relative(facing)) == net.minecraftforge.common.PlantType.PLAINS) {
            ResourceLocation registryName = state.getBlock().getRegistryName();
            if (registryName != null && state.getBlock() instanceof PanelBlock && registryName.getPath().startsWith("arbored")) {
                cir.setReturnValue(state.isFaceSturdy(world, pos.below(), Direction.UP));
            }
        }
    }
}