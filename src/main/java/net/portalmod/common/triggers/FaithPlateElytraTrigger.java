package net.portalmod.common.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

public class FaithPlateElytraTrigger extends AbstractCriterionTrigger<FaithPlateElytraTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(PortalMod.MODID, "faith_plate_elytra");

    public ResourceLocation getId() {
        return ID;
    }

    protected FaithPlateElytraTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entity, ConditionArrayParser condition) {
        return new FaithPlateElytraTrigger.Instance(entity);
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, x -> true);
    }

    public static class Instance extends CriterionInstance {
        public Instance(EntityPredicate.AndPredicate player) {
            super(FaithPlateElytraTrigger.ID, player);
        }
    }
}