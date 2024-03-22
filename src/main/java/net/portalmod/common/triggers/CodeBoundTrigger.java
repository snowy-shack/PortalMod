package net.portalmod.common.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

public class CodeBoundTrigger extends AbstractCriterionTrigger<CodeBoundTrigger.Instance> {
    private final ResourceLocation id;

    public CodeBoundTrigger(String name) {
        this.id = new ResourceLocation(PortalMod.MODID, name);
    }

    public ResourceLocation getId() {
        return id;
    }

    protected CodeBoundTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entity, ConditionArrayParser condition) {
        return new CodeBoundTrigger.Instance(this.id, entity);
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, x -> true);
    }

    public static class Instance extends CriterionInstance {
        public Instance(ResourceLocation id, EntityPredicate.AndPredicate player) {
            super(id, player);
        }
    }
}