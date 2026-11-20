package net.portalmod.common.triggers;

import net.minecraft.advancements.critereon.ContextAwarePredicate;

import com.google.gson.JsonObject;
import net.portalmod.PortalMod;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

public class ShootMoonTrigger extends SimpleCriterionTrigger<ShootMoonTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(PortalMod.MODID, "shoot_moon");

    public ResourceLocation getId() {
        return ID;
    }

    protected ShootMoonTrigger.Instance createInstance(JsonObject json, ContextAwarePredicate entity, DeserializationContext condition) {
        return new ShootMoonTrigger.Instance(entity);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, x -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ContextAwarePredicate player) {
            super(ShootMoonTrigger.ID, player);
        }
    }
}