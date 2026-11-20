package net.portalmod.common.triggers;

import com.google.gson.JsonObject;
import net.portalmod.PortalMod;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

public class FaithPlateElytraTrigger extends SimpleCriterionTrigger<FaithPlateElytraTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(PortalMod.MODID, "faith_plate_elytra");

    public ResourceLocation getId() {
        return ID;
    }

    protected FaithPlateElytraTrigger.Instance createInstance(JsonObject json, EntityPredicate.Composite entity, DeserializationContext condition) {
        return new FaithPlateElytraTrigger.Instance(entity);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, x -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(EntityPredicate.Composite player) {
            super(FaithPlateElytraTrigger.ID, player);
        }
    }
}