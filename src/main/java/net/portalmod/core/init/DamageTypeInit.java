package net.portalmod.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.portalmod.PortalMod;

/**
 * Damage types are data-driven since 1.19.4: each one is a JSON file in
 * data/portalmod/damage_type/, and code looks it up by key.
 */
public class DamageTypeInit {
    private DamageTypeInit() {}

    public static final ResourceKey<DamageType> GOO =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PortalMod.MODID, "goo"));

    public static DamageSource goo(Level level) {
        return level.damageSources().source(GOO);
    }
}
