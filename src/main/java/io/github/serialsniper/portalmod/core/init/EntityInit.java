package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.entities.AbstractCube;
import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, PortalMod.MODID);
    
    public static final RegistryObject<EntityType<AbstractCube>> COMPANION_CUBE = ENTITIES.register("companion_cube",
            () -> EntityType.Builder.of(AbstractCube::new, EntityClassification.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "companion_cube").toString()));
    
    public static final RegistryObject<EntityType<AbstractCube>> STORAGE_CUBE = ENTITIES.register("storage_cube",
            () -> EntityType.Builder.of(AbstractCube::new, EntityClassification.AMBIENT)
                    .sized(.8f, .8f)
                    .build(new ResourceLocation(PortalMod.MODID, "storage_cube").toString()));
    
    public static final RegistryObject<EntityType<CreeperEntity>> CREER = ENTITIES.register("creer",
            () -> EntityType.Builder.of(CreeperEntity::new, EntityClassification.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(8)
                    .build(new ResourceLocation(PortalMod.MODID, "creer").toString()));
    
    public static final RegistryObject<EntityType<PortalEntity>> PORTAL = ENTITIES.register("portal",
            () -> EntityType.Builder.<PortalEntity>of(PortalEntity::new, EntityClassification.MISC)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .setCustomClientFactory((spawnEntity, level) -> new PortalEntity(level))
                    .build(new ResourceLocation(PortalMod.MODID, "portal").toString()));
    
//    public static final RegistryObject<EntityType<PortalEntity>> PORTAL = null;
}