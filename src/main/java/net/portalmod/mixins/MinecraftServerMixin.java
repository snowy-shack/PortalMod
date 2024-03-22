package net.portalmod.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.portalmod.common.sorted.portal.PortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(
            remap = false,
            method = "loadLevel",
            at = @At("TAIL")
    )
    private void loadPortals(CallbackInfo info) {
        PortalManager.clear();
        ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD).getDataStorage()
                .get(PortalManager::getInstance, PortalManager.PATH);
    }
}