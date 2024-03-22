package net.portalmod.core.init;

import net.minecraft.util.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.registries.*;
import net.portalmod.PortalMod;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PortalMod.MODID);

    private SoundInit() {}

    public static final RegistryObject<SoundEvent> RADIO_LOOP = register("radio.loop");
    public static final RegistryObject<SoundEvent> RADIO_DINOSAUR1 = register("radio.dinosaur1");

    public static final RegistryObject<SoundEvent> GEL_BLOCK_BREAK = register("gel.grab");
    public static final RegistryObject<SoundEvent> GEL_BLOCK_PLACE = register("gel.place");

    public static final RegistryObject<SoundEvent> FAITHPLATE_LAUNCH = register("block.faithplate.launch");
    
    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(PortalMod.MODID, id)));
    }
}