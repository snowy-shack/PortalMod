package net.portalmod.common.sorted.trigger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.IStringSerializable;

import java.util.function.Predicate;

public enum TriggerType implements IStringSerializable {
    PLAYER("player", entity -> entity instanceof PlayerEntity),
    MOB("mob", entity -> !(entity instanceof PlayerEntity));

    private final String name;
    private final Predicate<LivingEntity> predicate;

    TriggerType(String name, Predicate<LivingEntity> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public Predicate<LivingEntity> getPredicate() {
        return predicate;
    }
}
