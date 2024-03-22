package net.portalmod.common.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

public class ShootMoonTrigger extends AbstractCriterionTrigger<ShootMoonTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(PortalMod.MODID, "shoot_moon");

    public ResourceLocation getId() {
        return ID;
    }

    protected ShootMoonTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entity, ConditionArrayParser condition) {
        return new ShootMoonTrigger.Instance(entity);
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, x -> true);
    }

    public static class Instance extends CriterionInstance {
        public Instance(EntityPredicate.AndPredicate player) {
            super(ShootMoonTrigger.ID, player);
        }
    }
}