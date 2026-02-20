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
    public static final RegistryObject<SoundEvent> RADIO_DINOSAUR1 = register("block.radio.transmission");

    public static final RegistryObject<SoundEvent> GEL_BREAK = register("block.gel.break");
    public static final RegistryObject<SoundEvent> GEL_COLLECT = register("block.gel.collect");
    public static final RegistryObject<SoundEvent> GEL_PLACE = register("block.gel.place");
    public static final RegistryObject<SoundEvent> GEL_STEP = register("block.gel.step");
    public static final RegistryObject<SoundEvent> REPULSION_GEL_BOUNCE = register("block.gel.bounce");

    public static final RegistryObject<SoundEvent> CHAMBER_DOOR_OPEN = register("block.chamber_door.open");
    public static final RegistryObject<SoundEvent> CHAMBER_DOOR_CLOSE = register("block.chamber_door.close");

    public static final RegistryObject<SoundEvent> CHAMBER_LIGHTS_AMBIENT = register("block.chamber_lights.hum");
    public static final RegistryObject<SoundEvent> CHAMBER_LIGHTS_FLICKER = register("block.chamber_lights.flicker");

    public static final RegistryObject<SoundEvent> PUSH_DOOR_OPEN = register("block.push_door.open");
    public static final RegistryObject<SoundEvent> PUSH_DOOR_CLOSE = register("block.push_door.close");

    public static final RegistryObject<SoundEvent> CUBE_DROPPER_OPEN = register("block.cube_dropper.open");
    public static final RegistryObject<SoundEvent> CUBE_DROPPER_CLOSE = register("block.cube_dropper.close");

    public static final RegistryObject<SoundEvent> BUTTON_ACTIVATE = register("block.button.activate");
    public static final RegistryObject<SoundEvent> BUTTON_DEACTIVATE = register("block.button.deactivate");
    public static final RegistryObject<SoundEvent> STANDING_BUTTON_PRESS = register("block.standing_button.press");
    public static final RegistryObject<SoundEvent> STANDING_BUTTON_RELEASE = register("block.standing_button.release");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_PRESS = register("block.super_button.press");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_RELEASE = register("block.super_button.release");

    public static final RegistryObject<SoundEvent> FAITHPLATE_LAUNCH = register("block.faithplate.launch");
    public static final RegistryObject<SoundEvent> FAITHPLATE_RESET = register("block.faithplate.reset");

    public static final RegistryObject<SoundEvent> FIZZLER_ACTIVATE = register("block.fizzler_emitter.activate");
    public static final RegistryObject<SoundEvent> FIZZLER_DEACTIVATE = register("block.fizzler_emitter.deactivate");

    public static final RegistryObject<SoundEvent> ANTLINE_INDICATOR_ACTIVATE = register("block.antline_indicator.activate");
    public static final RegistryObject<SoundEvent> ANTLINE_INDICATOR_DEACTIVATE = register("block.antline_indicator.deactivate");
    public static final RegistryObject<SoundEvent> ANTLINE_TIMER_TICK = register("block.antline_timer.tick");

    public static final RegistryObject<SoundEvent> CAKE_EAT_CANDLE = register("block.forest_cake.eat_candle");

    // ITEM
    public static final RegistryObject<SoundEvent> PORTALGUN_FIRE_PRIMARY = register("item.portalgun.fire_primary");
    public static final RegistryObject<SoundEvent> PORTALGUN_FIRE_SECONDARY = register("item.portalgun.fire_secondary");
    public static final RegistryObject<SoundEvent> PORTALGUN_FIZZLE = register("item.portalgun.fizzle");
    public static final RegistryObject<SoundEvent> PORTALGUN_LIFT = register("item.portalgun.lift");
    public static final RegistryObject<SoundEvent> PORTALGUN_HOLD = register("item.portalgun.hold");
    public static final RegistryObject<SoundEvent> PORTALGUN_DROP = register("item.portalgun.drop");

    public static final RegistryObject<SoundEvent> BUCKET_FILL_GOO = register("item.bucket.fill_goo");
    public static final RegistryObject<SoundEvent> BUCKET_EMPTY_GOO = register("item.bucket.empty_goo");

    public static final RegistryObject<SoundEvent> WRENCH_USE = register("item.wrench.use");

    // ENTITY
    public static final RegistryObject<SoundEvent> PORTAL_OPEN = register("entity.portal.open");
    public static final RegistryObject<SoundEvent> PORTAL_TELEPORT = register("entity.portal.teleport"); // TODO: use me
    public static final RegistryObject<SoundEvent> GOO_DAMAGE = register("entity.player.hurt_goo");

    public static final RegistryObject<SoundEvent> ENTITY_FIZZLE = register("entity.fizzle");

    public static final RegistryObject<SoundEvent> CUBE_HIT = register("entity.cube.hit");
    public static final RegistryObject<SoundEvent> CUBE_GABE = register("entity.cube.gabe");

    public static final RegistryObject<SoundEvent> TURRET_OPEN = register("entity.turret.open");
    public static final RegistryObject<SoundEvent> TURRET_CLOSE = register("entity.turret.close");
    public static final RegistryObject<SoundEvent> TURRET_FIRE = register("entity.turret.fire");
    public static final RegistryObject<SoundEvent> TURRET_FIRE_FAIL = register("entity.turret.fire_fail");
    public static final RegistryObject<SoundEvent> TURRET_STOCK = register("entity.turret.stock");

    public static final RegistryObject<SoundEvent> CHAMBER_SIGN_PLACE = register("entity.chamber_sign.place");

    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(PortalMod.MODID, id)));
    }
}