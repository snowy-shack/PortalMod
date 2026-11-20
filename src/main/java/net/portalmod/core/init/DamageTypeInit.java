package net.portalmod.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.portalmod.PortalMod;

/**
 * Damage types are data-driven since 1.19.4: each one is a JSON file in
 * data/portalmod/damage_type/, and code looks it up by key.
 */
public class DamageTypeInit {
    private DamageTypeInit() {}

    public static final ResourceKey<DamageType> GOO = key("goo");
    public static final ResourceKey<DamageType> FIZZLE = key("fizzle");
    public static final ResourceKey<DamageType> CUBE = key("cube");
    public static final ResourceKey<DamageType> TURRET = key("turret");
    public static final ResourceKey<DamageType> FAITHPLATE_WALL = key("faithplate_wall");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PortalMod.MODID, name));
    }

    public static DamageSource goo(Level level) {
        return source(level, GOO);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return level.damageSources().source(type);
    }

    /** A damage source caused by an entity (was EntityDamageSource). */
    public static DamageSource source(Level level, ResourceKey<DamageType> type, Entity attacker) {
        return level.damageSources().source(type, attacker);
    }
}
