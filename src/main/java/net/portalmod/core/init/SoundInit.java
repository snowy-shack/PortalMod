package net.portalmod.core.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PortalMod.MODID);

    private SoundInit() {}

    // BLOCKS
    public static final RegistryObject<SoundEvent> RADIO_LOOP = register("block.radio.loop");
    public static final RegistryObject<SoundEvent> RADIO_DINOSAUR1 = register("block.radio.dinosaur");

    public static final RegistryObject<SoundEvent> GEL_BREAK = register("block.gel.break");
    public static final RegistryObject<SoundEvent> GEL_COLLECT = register("block.gel.collect");
    public static final RegistryObject<SoundEvent> GEL_PLACE = register("block.gel.place");
    public static final RegistryObject<SoundEvent> GEL_STEP = register("block.gel.step");;
    public static final RegistryObject<SoundEvent> REPULSION_GEL_BOUNCE = register("block.gel.bounce");
    public static final RegistryObject<SoundEvent> PROPULSION_GEL_ENTER = register("block.gel.enter");
    public static final RegistryObject<SoundEvent> PROPULSION_GEL_LEAVE = register("block.gel.leave");

    public static final RegistryObject<SoundEvent> CHAMBER_DOOR_OPEN = register("block.chamber_door.open");
    public static final RegistryObject<SoundEvent> CHAMBER_DOOR_CLOSE = register("block.chamber_door.close");

    public static final RegistryObject<SoundEvent> CUBE_DROPPER_OPEN = register("block.cube_dropper.open");
    public static final RegistryObject<SoundEvent> CUBE_DROPPER_CLOSE = register("block.cube_dropper.close");

    public static final RegistryObject<SoundEvent> BUTTON_ACTIVATE = register("block.super_button.activate");
    public static final RegistryObject<SoundEvent> BUTTON_DEACTIVATE = register("block.super_button.deactivate");
    public static final RegistryObject<SoundEvent> STANDING_BUTTON_PRESS = register("block.standing_button.press");
    public static final RegistryObject<SoundEvent> STANDING_BUTTON_RELEASE = register("block.standing_button.release");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_PRESS = register("block.super_button.press");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_RELEASE = register("block.super_button.release");

    public static final RegistryObject<SoundEvent> FAITHPLATE_LAUNCH = register("block.faithplate.launch");
    public static final RegistryObject<SoundEvent> FAITHPLATE_RESET = register("block.faithplate.reset");

    public static final RegistryObject<SoundEvent> FIZZLER_ACTIVATE = register("block.fizzler.activate");
    public static final RegistryObject<SoundEvent> FIZZLER_DEACTIVATE = register("block.fizzler.deactivate");

    public static final RegistryObject<SoundEvent> ANTLINE_INDICATOR_ACTIVATE = register("block.antline_indicator.activate");
    public static final RegistryObject<SoundEvent> ANTLINE_INDICATOR_DEACTIVATE = register("block.antline_indicator.deactivate");

    public static final RegistryObject<SoundEvent> BUCKET_FILL_GOO = register("item.bucket.fill_goo");
    public static final RegistryObject<SoundEvent> BUCKET_EMPTY_GOO = register("item.bucket.empty_goo");

    // ITEM
    public static final RegistryObject<SoundEvent> PORTALGUN_FIRE_PRIMARY = register("item.portalgun.fire_primary");
    public static final RegistryObject<SoundEvent> PORTALGUN_FIRE_SECONDARY = register("item.portalgun.fire_secondary");
    public static final RegistryObject<SoundEvent> PORTALGUN_FIZZLE = register("item.portalgun.fizzle");
    public static final RegistryObject<SoundEvent> PORTALGUN_HOLD = register("item.portalgun.hold");
    public static final RegistryObject<SoundEvent> PORTALGUN_DROP = register("item.portalgun.drop");

    public static final RegistryObject<SoundEvent> WRENCH_USE = register("item.wrench.use");

    // ENTITY
    public static final RegistryObject<SoundEvent> PORTAL_OPEN = register("entity.portal.open");
    public static final RegistryObject<SoundEvent> GOO_DAMAGE = register("entity.player.hurt_goo");

    public static final RegistryObject<SoundEvent> ENTITY_FIZZLE = register("entity.fizzle");

    public static final RegistryObject<SoundEvent> CUBE_HIT = register("entity.cube.hit");

    public static final RegistryObject<SoundEvent> TURRET_DEPLOY = register("entity.turret.deploy");
    public static final RegistryObject<SoundEvent> TURRET_RETRACT = register("entity.turret.retract");
    public static final RegistryObject<SoundEvent> TURRET_FIRE = register("entity.turret.fire");

    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(PortalMod.MODID, id)));
    }
}