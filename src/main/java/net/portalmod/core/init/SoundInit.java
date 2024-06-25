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

    public static final RegistryObject<SoundEvent> RADIO_LOOP = register("block.radio.loop");
    public static final RegistryObject<SoundEvent> RADIO_DINOSAUR1 = register("block.radio.dinosaur");

    public static final RegistryObject<SoundEvent> GEL_BLOCK_BREAK = register("block.gel.break");
    public static final RegistryObject<SoundEvent> GEL_BLOCK_COLLECT = register("block.gel.collect");
    public static final RegistryObject<SoundEvent> GEL_BLOCK_PLACE = register("block.gel.place");
    public static final RegistryObject<SoundEvent> GEL_BLOCK_STEP = register("block.gel.step");;
    public static final RegistryObject<SoundEvent> GEL_BLOCK_BOUNCE = register("block.gel.bounce");

    public static final RegistryObject<SoundEvent> CHAMBER_DOOR_OPEN = register("block.chamber_door.open");
    public static final RegistryObject<SoundEvent> CHAMBER_DOOR_CLOSE = register("block.chamber_door.close");

    public static final RegistryObject<SoundEvent> SUPER_BUTTON_PRESS = register("block.super_button.press");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_RELEASE = register("block.super_button.release");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_ACTIVATE = register("block.super_button.activate");
    public static final RegistryObject<SoundEvent> SUPER_BUTTON_DEACTIVATE = register("block.super_button.deactivate");

    public static final RegistryObject<SoundEvent> FAITHPLATE_LAUNCH = register("block.faithplate.launch");

    public static final RegistryObject<SoundEvent> BUCKET_FILL_GOO = register("item.bucket.fill_goo");
    public static final RegistryObject<SoundEvent> BUCKET_EMPTY_GOO = register("item.bucket.empty_goo");

    public static final RegistryObject<SoundEvent> PORTALGUN_FIZZLE = register("item.portalgun.fizzle");

    public static final RegistryObject<SoundEvent> GOO_DAMAGE = register("entity.player.hurt_goo");

    public static final RegistryObject<SoundEvent> ENTITY_FIZZLE = register("entity.fizzle");

    public static final RegistryObject<SoundEvent> CUBE_HIT = register("entity.cube.hit");

    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(PortalMod.MODID, id)));
    }
}