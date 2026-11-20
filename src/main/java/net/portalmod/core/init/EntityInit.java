package net.portalmod.core.init;

import net.portalmod.core.registry.RegistryObject;
import net.portalmod.core.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.sign.ChamberSignEntity;
import net.portalmod.common.sorted.turret.TurretEntity;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, PortalMod.MODID);

    private EntityInit() {}
    
    public static final RegistryObject<EntityType<Cube>> GABE = ENTITIES.register("gabe",
            () -> EntityType.Builder.of(Cube::new, MobCategory.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "gabe").toString()));

    public static final RegistryObject<EntityType<Cube>> COMPANION_CUBE = ENTITIES.register("companion_cube",
            () -> EntityType.Builder.of(Cube::new, MobCategory.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "companion_cube").toString()));
    
    public static final RegistryObject<EntityType<Cube>> STORAGE_CUBE = ENTITIES.register("storage_cube",
            () -> EntityType.Builder.of(Cube::new, MobCategory.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "storage_cube").toString()));

    public static final RegistryObject<EntityType<Cube>> VINTAGE_CUBE = ENTITIES.register("vintage_cube",
            () -> EntityType.Builder.of(Cube::new, MobCategory.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "vintage_cube").toString()));

    public static final RegistryObject<EntityType<TurretEntity>> TURRET = ENTITIES.register("turret",
            () -> EntityType.Builder.<TurretEntity>of(TurretEntity::new, MobCategory.CREATURE)
                    .build(new ResourceLocation(PortalMod.MODID, "turret").toString()));

    public static final RegistryObject<EntityType<ChamberSignEntity>> CHAMBER_SIGN = ENTITIES.register("chamber_sign",
            () -> EntityType.Builder.<ChamberSignEntity>of(ChamberSignEntity::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(new ResourceLocation(PortalMod.MODID, "chamber_sign").toString()));

//    public static final RegistryObject<EntityType<PelletEntity>> PELLET = ENTITIES.register("energy_pellet",
//            () -> EntityType.Builder.<PelletEntity>of(PelletEntity::new, MobCategory.MISC)
//                    .sized(.4f, .4f)
//                    .build(new ResourceLocation(PortalMod.MODID, "energy_pellet").toString()));
    
//    public static final RegistryObject<EntityType<CreeperEntity>> CREER = ENTITIES.register("creer",
//            () -> EntityType.Builder.of(CreeperEntity::new, MobCategory.MONSTER)
//                    .sized(0.6F, 1.7F)
//                    .clientTrackingRange(8)
//                    .build(new ResourceLocation(PortalMod.MODID, "creer").toString()));
    
    public static final RegistryObject<EntityType<PortalEntity>> PORTAL = ENTITIES.register("portal",
            () -> EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .setCustomClientFactory((spawnEntity, level) -> new PortalEntity(level))
                    .build(new ResourceLocation(PortalMod.MODID, "portal").toString()));
    
//    public static final RegistryObject<EntityType<PortalEntity>> PORTAL = null;
}