package net.portalmod.core.init;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.sign.ChamberSignEntity;
import net.portalmod.common.sorted.turret.TurretEntity;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, PortalMod.MODID);

    private EntityInit() {}
    
    public static final RegistryObject<EntityType<Cube>> GABE = ENTITIES.register("gabe",
            () -> EntityType.Builder.of(Cube::new, EntityClassification.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "gabe").toString()));

    public static final RegistryObject<EntityType<Cube>> COMPANION_CUBE = ENTITIES.register("companion_cube",
            () -> EntityType.Builder.of(Cube::new, EntityClassification.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "companion_cube").toString()));
    
    public static final RegistryObject<EntityType<Cube>> STORAGE_CUBE = ENTITIES.register("storage_cube",
            () -> EntityType.Builder.of(Cube::new, EntityClassification.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "storage_cube").toString()));

    public static final RegistryObject<EntityType<Cube>> VINTAGE_CUBE = ENTITIES.register("vintage_cube",
            () -> EntityType.Builder.of(Cube::new, EntityClassification.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "vintage_cube").toString()));

    public static final RegistryObject<EntityType<TurretEntity>> TURRET = ENTITIES.register("turret",
            () -> EntityType.Builder.<TurretEntity>of(TurretEntity::new, EntityClassification.CREATURE)
                    .sized(.8f, 1.3f)
                    .build(new ResourceLocation(PortalMod.MODID, "turret").toString()));

    public static final RegistryObject<EntityType<ChamberSignEntity>> CHAMBER_SIGN = ENTITIES.register("chamber_sign",
            () -> EntityType.Builder.<ChamberSignEntity>of(ChamberSignEntity::new, EntityClassification.MISC)
                    .sized(.5f, .5f)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(new ResourceLocation(PortalMod.MODID, "chamber_sign").toString()));
    
//    public static final RegistryObject<EntityType<CreeperEntity>> CREER = ENTITIES.register("creer",
//            () -> EntityType.Builder.of(CreeperEntity::new, EntityClassification.MONSTER)
//                    .sized(0.6F, 1.7F)
//                    .clientTrackingRange(8)
//                    .build(new ResourceLocation(PortalMod.MODID, "creer").toString()));
    
    public static final RegistryObject<EntityType<PortalEntity>> PORTAL = ENTITIES.register("portal",
            () -> EntityType.Builder.<PortalEntity>of(PortalEntity::new, EntityClassification.MISC)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .setCustomClientFactory((spawnEntity, level) -> new PortalEntity(level))
                    .build(new ResourceLocation(PortalMod.MODID, "portal").toString()));
    
//    public static final RegistryObject<EntityType<PortalEntity>> PORTAL = null;
}