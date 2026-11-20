package net.portalmod.common.sorted.trigger;

import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.StringRepresentable;

public enum TriggerType implements StringRepresentable {
    PLAYER("player", entity -> entity instanceof Player),
    MOB("mob", entity -> !(entity instanceof Player));

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
