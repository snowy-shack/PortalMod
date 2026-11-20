package net.portalmod.common.triggers;

import com.google.gson.JsonObject;

import net.portalmod.PortalMod;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class ClickMoonCriteriaTrigger implements CriterionTrigger<ClickMoonCriteriaTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(PortalMod.MODID, "click_moon");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements p_192165_1_, Listener<Instance> p_192165_2_) {

    }

    @Override
    public void removePlayerListener(PlayerAdvancements p_192164_1_, Listener<Instance> p_192164_2_) {

    }

    @Override
    public void removePlayerListeners(PlayerAdvancements p_192167_1_) {

    }

    @Override
    public Instance createInstance(JsonObject p_230307_1_, DeserializationContext p_230307_2_) {
        return null;
    }

    public static class Instance implements CriterionTriggerInstance {
        public ResourceLocation getCriterion() {
            return ClickMoonCriteriaTrigger.ID;
        }

        public JsonObject serializeToJson(SerializationContext p_230240_1_) {
            return new JsonObject();
        }
    }
}