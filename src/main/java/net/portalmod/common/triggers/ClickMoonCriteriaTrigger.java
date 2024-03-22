package net.portalmod.common.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

public class ClickMoonCriteriaTrigger implements ICriterionTrigger<ClickMoonCriteriaTrigger.Instance> {
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
    public Instance createInstance(JsonObject p_230307_1_, ConditionArrayParser p_230307_2_) {
        return null;
    }

    public static class Instance implements ICriterionInstance {
        public ResourceLocation getCriterion() {
            return ClickMoonCriteriaTrigger.ID;
        }

        public JsonObject serializeToJson(ConditionArraySerializer p_230240_1_) {
            return new JsonObject();
        }
    }
}