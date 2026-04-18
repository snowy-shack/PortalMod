package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalHandler;
import net.portalmod.core.init.CriteriaTriggerInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(EnderPearlEntity.class)
public class EnderPearlMixin implements PortalHandler {
    @Unique
    private final Set<UUID> portalmod_passedPortals = new HashSet<>();

    @Inject(
                        method = "onHit",
            at = @At("HEAD")
    )
    private void pmOnHit(RayTraceResult rtr, CallbackInfo ci) {
        Entity owner = ((EnderPearlEntity)(Object)this).getOwner();

        if(portalmod_passedPortals.size() == 2 && owner instanceof ServerPlayerEntity) {
            CriteriaTriggerInit.TRIPLE_TELEPORT.get().trigger((ServerPlayerEntity)owner);
        }
    }

    @Override
    public void onTeleport(PortalEntity from, PortalEntity to) {
        portalmod_passedPortals.add(from.getGunUUID());
    }

    @Override
    public void onTeleportPacket() {}
}