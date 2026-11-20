package net.portalmod.mixins.entity;

import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalHandler;
import net.portalmod.core.init.CriteriaTriggerInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;

@Mixin(ThrownEnderpearl.class)
public class EnderPearlMixin implements PortalHandler {
    @Unique
    private final Set<UUID> portalmod_passedPortals = new HashSet<>();

    @Inject(
                        method = "onHit",
            at = @At("HEAD")
    )
    private void pmOnHit(HitResult rtr, CallbackInfo ci) {
        Entity owner = ((ThrownEnderpearl)(Object)this).getOwner();

        if(portalmod_passedPortals.size() == 2 && owner instanceof ServerPlayer) {
            CriteriaTriggerInit.TRIPLE_TELEPORT.get().trigger((ServerPlayer)owner);
        }
    }

    @Override
    public void onTeleport(PortalEntity from, PortalEntity to) {
        portalmod_passedPortals.add(from.getGunUUID());
    }

    @Override
    public void onTeleportPacket() {}
}