package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import net.minecraft.util.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.registries.*;

public class SoundInit {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PortalMod.MODID);
	
	public static final RegistryObject<SoundEvent> RADIO_LOOP = SOUNDS.register("radio.loop",
			() -> new SoundEvent(new ResourceLocation(PortalMod.MODID, "radio.loop")));

	public static final RegistryObject<SoundEvent> RADIO_DINOSAUR1 = SOUNDS.register("radio.dinosaur1",
			() -> new SoundEvent(new ResourceLocation(PortalMod.MODID, "radio.dinosaur1")));
}