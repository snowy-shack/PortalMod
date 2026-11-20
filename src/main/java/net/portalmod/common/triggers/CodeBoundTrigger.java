package net.portalmod.common.triggers;

import com.google.gson.JsonObject;
import net.portalmod.PortalMod;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

public class CodeBoundTrigger extends SimpleCriterionTrigger<CodeBoundTrigger.Instance> {
    private final ResourceLocation id;

    public CodeBoundTrigger(String name) {
        this.id = new ResourceLocation(PortalMod.MODID, name);
    }

    public ResourceLocation getId() {
        return id;
    }

    protected CodeBoundTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entity, DeserializationContext condition) {
        return new CodeBoundTrigger.Instance(this.id, entity);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, x -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ResourceLocation id, EntityPredicate.AndPredicate player) {
            super(id, player);
        }
    }
}